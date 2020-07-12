package stickyWallet.sql

import org.bukkit.ChatColor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
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
import java.util.UUID

object PostgresHandler : UsePlugin, DataHandler("postgres") {
    private lateinit var dbConnection: Database

    override fun initialize() {
        dbConnection = Database.connect(
            "jdbc:postgresql://${StorageSettings.storageHost}:${StorageSettings.storagePort}/${StorageSettings.storageDatabase}",
            driver = "org.postgresql.Driver",
            user = StorageSettings.storageUsername,
            password = StorageSettings.storagePassword
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                AccountsTable,
                BalancesTable,
                CurrenciesTable
            )
        }
    }

    override fun getTopList(currency: Currency, offset: Long, amount: Int): Map<String, Double>? {
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
                        Pair(it[AccountsTable.playerName], it[BalancesTable.balance])
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
            transaction {
                CurrenciesTable.select { (CurrenciesTable.type inList StorageSettings.currencyTypes) }
                    .map { rowToCurrency(it) }
                    .forEach {
                        pluginInstance.currencyStore.addCachedCurrency(it)
                        pluginInstance.logger.info("Loaded currency: ${it.plural} of type ${it.type}")
                    }
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
                    it[color] = currency.color.toString()
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
                CurrenciesTable.select { (CurrenciesTable.uuid eq currency.uuid.toString()) }.firstOrNull()?.let { rowToCurrency(it) }
            } ?: throw IllegalStateException("Failed to find currency with UUID: ${currency.uuid}, yet we were expected to update cached")

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

    override fun loadAccount(playerName: String): Account? = sharedLoadAccount(playerName)

    override fun loadAccount(uuid: UUID): Account? = sharedLoadAccount(uuid.toString())

    private fun sharedLoadAccount(id: String): Account? {
        var account: Account? = null

        try {
            account = transaction {
                AccountsTable.select { (AccountsTable.playerName eq id) or (AccountsTable.playerUUID eq id) }.firstOrNull()?.let { rowToAccount(it) }
            }
            account?.let {
                val balRows = transaction {
                    BalancesTable.select {
                        (BalancesTable.accountID eq it.uuid.toString()) and
                        (BalancesTable.currencyID inList pluginInstance.currencyStore.currencies.map { curr -> curr.uuid.toString() })
                    }
                        .map {
                            Pair(pluginInstance.currencyStore.getCurrency(UUID.fromString(it[BalancesTable.currencyID])), it[BalancesTable.balance])
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