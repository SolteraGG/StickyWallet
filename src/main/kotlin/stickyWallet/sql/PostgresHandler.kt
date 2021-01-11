package stickyWallet.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.ChatColor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConnectionAutoRegistration
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import pw.forst.exposed.insertOrUpdate
import stickyWallet.accounts.Account
import stickyWallet.bungee.UpdateForwarder
import stickyWallet.configs.PluginConfiguration.StorageSettings
import stickyWallet.currencies.Currency
import stickyWallet.interfaces.DataHandler
import stickyWallet.interfaces.UsePlugin
import stickyWallet.sql.tables.AccountsTable
import stickyWallet.sql.tables.BalancesTable
import stickyWallet.sql.tables.CurrenciesTable
import java.math.BigDecimal
import java.util.ServiceLoader
import java.util.UUID

object PostgresHandler : UsePlugin, DataHandler("postgres") {
    private lateinit var dbConnection: Database

    override fun initialize() {
        val bukkitClassLoader = Database::class.java.classLoader
        val bukkitServiceLoader = ServiceLoader.load(DatabaseConnectionAutoRegistration::class.java,bukkitClassLoader)
        println("[Bukkit] ClassLoader: $bukkitClassLoader ServiceLoader: ${bukkitServiceLoader.toList()}")

        val contextClassLoader = Thread.currentThread().contextClassLoader
        val contextServiceLoader = ServiceLoader.load(DatabaseConnectionAutoRegistration::class.java,contextClassLoader)
        println("[Context] ClassLoader: $contextClassLoader ServiceLoader: ${contextServiceLoader.toList()}")


        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://${StorageSettings.storageHost}:${StorageSettings.storagePort}/${StorageSettings.storageDatabase}"
            driverClassName = "stickyWallet.org.postgresql.Driver"
            username = StorageSettings.storageUsername
            password = StorageSettings.storagePassword
            maximumPoolSize = 2
        }

        val dataSource = HikariDataSource(config)
        //
        // val conn = dataSource.connection
        // val statement = conn.prepareStatement("""
        //     select * from stickywallet_accounts
        // """.trimIndent())
        // val result = statement.executeQuery()
        //
        // val columns = result.metaData.columnCount
        //
        // while (result.next()) {
        //     for (i in 1..columns) {
        //         println(result.metaData.getColumnName(i) + ": " + result.getString(i))
        //     }
        //     println()
        // }
        //
        // conn.close()

        dbConnection = Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                AccountsTable,
                BalancesTable,
                CurrenciesTable
            )
        }
    }

    override fun getTopList(currency: Currency, offset: Long, amount: Int): Map<String, BigDecimal> {
        try {
            val pairs = transaction {
                BalancesTable.join(
                    AccountsTable,
                    JoinType.INNER,
                    additionalConstraint = {
                        (BalancesTable.accountID eq AccountsTable.playerUUID) and
                            (BalancesTable.currencyID eq currency.uuid.toString())
                    }
                )
                    .slice(AccountsTable.playerName, BalancesTable.balance)
                    .selectAll()
                    .orderBy(BalancesTable.balance to SortOrder.DESC)
                    .limit(amount, offset = offset)
                    .map {
                        it[AccountsTable.playerName] to it[BalancesTable.balance]
                    }
            }

            return pairs.toMap()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return mapOf()
        }
    }

    override fun loadCurrencies() {
        try {
            val loadedCurrencies = transaction {
                CurrenciesTable.select { (CurrenciesTable.type inList StorageSettings.currencyTypes) }
                    .map { rowToCurrency(it) }
            }

            loadedCurrencies
                .forEach {
                    if (loadedCurrencies.size == 1) it.defaultCurrency = true
                    pluginInstance.currencyStore.addCachedCurrency(it)
                    pluginInstance.logger.info("Loaded currency: ${it.plural} of type ${it.type}")
                }

            if (pluginInstance.currencyStore.currencies.size == 1 || !pluginInstance.currencyStore.currencies.any { it.defaultCurrency }) {
                pluginInstance.currencyStore.currencies.first().defaultCurrency = true
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun saveCurrency(currency: Currency) {
        try {
            transaction {
                CurrenciesTable.insertOrUpdate(CurrenciesTable.singular, CurrenciesTable.plural) {
                    it[uuid] = currency.uuid.toString()
                    it[type] = currency.type
                    it[singular] = currency.singular
                    it[plural] = currency.plural
                    it[symbol] = currency.symbol
                    it[color] = currency.color.char.toString()
                    it[decimalSupported] = currency.decimalSupported
                    it[defaultCurrency] = currency.defaultCurrency
                    it[defaultBalance] = currency.defaultBalance
                    it[exchangeRate] = currency.exchangeRate
                }
            }

            UpdateForwarder.sendUpdateMessage("currency", currency.uuid.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun deleteCurrency(currency: Currency) {
        try {
            transaction {
                CurrenciesTable.deleteWhere { (CurrenciesTable.uuid eq currency.uuid.toString()) }
                BalancesTable.deleteWhere { (BalancesTable.currencyID eq currency.uuid.toString()) }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun updateCachedCurrency(currency: Currency) {
        try {
            val result = transaction {
                CurrenciesTable.select { (CurrenciesTable.uuid eq currency.uuid.toString()) }.firstOrNull()
                    ?.let { rowToCurrency(it) }
            }
                ?: throw IllegalStateException("Failed to find currency with UUID: ${currency.uuid}, yet we were expected to update cached")

            currency.apply {
                symbol = result.symbol
                color = result.color
                decimalSupported = result.decimalSupported
                defaultCurrency = result.defaultCurrency
                defaultBalance = result.defaultBalance
                exchangeRate = result.exchangeRate
            }

            pluginInstance.logger.info("Updated cached currency ${currency.plural}")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun loadAccount(playerName: String): Account? {
        pluginLogger.info("Called loadAccount with playerName: $playerName")
        return sharedLoadAccount(playerName)
    }

    override fun loadAccount(uuid: UUID): Account? {
        pluginLogger.info("Called loadAccount with UUID $uuid")
        return sharedLoadAccount(uuid.toString())
    }

    private fun sharedLoadAccount(id: String): Account? {
        pluginLogger.info("Called sharedLoadAccount with id: $id")

        var account: Account? = null

        // Extremely crude stripping of the two characters that MAY affect ilike queries
        val accName = if (id.contains("%") || id.contains("_")) {
            id.replace("%", "")
                .replace("_", "")
        } else {
            id
        }

        try {
            account = transaction {
                addLogger(StdOutSqlLogger)
                AccountsTable.select { (AccountsTable.playerName ilike accName) or (AccountsTable.playerUUID eq id) }
                    .firstOrNull()?.let { rowToAccount(it) }
            }
            account?.let {
                val balRows = transaction {
                    BalancesTable.select {
                        (BalancesTable.accountID eq it.uuid.toString()) and
                            (BalancesTable.currencyID inList pluginInstance.currencyStore.currencies.map { curr -> curr.uuid.toString() })
                    }
                        .map {
                            pluginInstance.currencyStore.getCurrency(UUID.fromString(it[BalancesTable.currencyID])) to it[BalancesTable.balance]
                        }
                }

                for ((currency, amount) in balRows) {
                    if (currency == null) continue
                    account.modifyCurrencyBalance(currency, amount, save = false)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return account
    }

    override fun saveAccount(account: Account) {
        try {
            transaction {
                AccountsTable.insertOrUpdate(AccountsTable.playerUUID) {
                    it[playerName] = account.playerName ?: "Unknown Player"
                    it[playerUUID] = account.uuid.toString()
                }

                commit()

                pluginInstance.currencyStore.currencies.forEach { curr ->
                    BalancesTable.insertOrUpdate(BalancesTable.accountID, BalancesTable.currencyID) {
                        it[accountID] = account.uuid.toString()
                        it[currencyID] = curr.uuid.toString()
                        it[balance] = account.getBalanceForCurrency(curr)
                    }
                }
                commit()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        UpdateForwarder.sendUpdateMessage("account", account.uuid.toString())
    }

    override fun deleteAccount(account: Account) {
        try {
            transaction {
                AccountsTable.deleteWhere { (AccountsTable.playerUUID eq account.uuid.toString()) }
                BalancesTable.deleteWhere { (BalancesTable.accountID eq account.uuid.toString()) }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun rowToCurrency(row: ResultRow) = Currency(
        uuid = UUID.fromString(row[CurrenciesTable.uuid]),
        type = row[CurrenciesTable.type],
        singular = row[CurrenciesTable.singular],
        plural = row[CurrenciesTable.plural],
        symbol = row[CurrenciesTable.symbol],
        color = ChatColor.getByChar(row[CurrenciesTable.color]) ?: ChatColor.WHITE,
        decimalSupported = row[CurrenciesTable.decimalSupported],
        defaultCurrency = row[CurrenciesTable.defaultCurrency],
        defaultBalance = row[CurrenciesTable.defaultBalance],
        exchangeRate = row[CurrenciesTable.exchangeRate]
    )

    private fun rowToAccount(row: ResultRow) = Account(
        uuid = UUID.fromString(row[AccountsTable.playerUUID]),
        playerName = row[AccountsTable.playerName]
    )
}
