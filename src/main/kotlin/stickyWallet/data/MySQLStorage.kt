package stickyWallet.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.UUID
import org.bukkit.ChatColor
import stickyWallet.StickyPlugin
import stickyWallet.accounts.Account
import stickyWallet.currency.Currency
import stickyWallet.utils.SQLStatements
import stickyWallet.utils.SQLStatements.AccountParams
import stickyWallet.utils.SQLStatements.BalanceParams
import stickyWallet.utils.SQLStatements.CurrencyParams
import stickyWallet.utils.ServerUtils

class MySQLStorage(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) : DataStore("mysql", true) {

    private val hikariConfig = HikariConfig()
    private lateinit var hikari: HikariDataSource
    private val tablePrefix
        get() = StickyPlugin.instance.config.getString("mysql.tableprefix", "stickywallet")!!

    private val cachedTopLists = mutableMapOf<UUID, CachedTopList>()

    init {
        hikariConfig.jdbcUrl = "jdbc:mysql://$host:$port/$database?allowPublicKeyRetrieval=true&useSSL=false"
        hikariConfig.username = username
        hikariConfig.password = password
        hikariConfig.maximumPoolSize = 12
        hikariConfig.connectionTimeout = 60000
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hikariConfig.addDataSourceProperty("userServerPrepStmts", "true")
    }

    @Throws(SQLException::class)
    private fun setupTables(connection: Connection) {
        connection.prepareStatement(
            SQLStatements.CREATE_TABLE_ACCOUNTS(tablePrefix)
        )
            .execute()
        connection.prepareStatement(
            SQLStatements.CREATE_TABLE_CURRENCIES(tablePrefix)
        )
            .execute()
        connection.prepareStatement(
            SQLStatements.CREATE_TABLE_BALANCES(tablePrefix)
        )
            .execute()

        // Constraint check

        val map = mapOf(
            "UniqueCurrency UNIQUE (name_singular, name_plural)" to SQLStatements.SELECT_CONSTRAINT_FOR_TABLE("UniqueCurrency"),
            "UniqueAccount UNIQUE (uuid)" to SQLStatements.SELECT_CONSTRAINT_FOR_TABLE("UniqueAccount"),
            "UniqueBalance UNIQUE (account_id, currency_id)" to SQLStatements.SELECT_CONSTRAINT_FOR_TABLE("UniqueBalance")
        )

        map.forEach { (constraint, selectConstraint) ->
            val result = connection.prepareStatement(selectConstraint).executeQuery()
            if (!result.next()) {
                connection.prepareStatement(
                    SQLStatements.ALTER_TABLE_ADD_UNIQUE_CONSTRAINT(
                        when {
                            constraint.startsWith("UniqueCurrency") -> "${tablePrefix}_currencies"
                            constraint.startsWith("UniqueAccount") -> "${tablePrefix}_accounts"
                            else -> "${tablePrefix}_balances"
                        },
                        constraint
                    )
                ).execute()
            }
        }
    }

    @Throws(SQLException::class)
    override fun initialize() {
        hikari = HikariDataSource(hikariConfig)
        val connection = hikari.connection
        setupTables(connection)
        connection.close()
    }

    override fun close() {
        hikari.close()
    }

    override fun loadCurrencies() {
        try {
            val connection = hikari.connection

            try {
                val result = connection.prepareStatement(
                        SQLStatements.SELECT_ALL_CURRENCIES(tablePrefix)
                ).executeQuery()

                while (result.next()) {
                    val uuid = UUID.fromString(result.getString(CurrencyParams.uuid))
                    val singular = result.getString(CurrencyParams.singular)
                    val plural = result.getString(CurrencyParams.plural)
                    val defaultBalance = result.getDouble(CurrencyParams.defaultBalance)
                    val symbol = result.getString(CurrencyParams.symbol)
                    val decimalSupported = result.getInt(CurrencyParams.decimalsSupported) == 1
                    val defaultCurrency = result.getInt(CurrencyParams.defaultCurrency) == 1
                    val payable = result.getInt(CurrencyParams.payable) == 1
                    val color = ChatColor.valueOf(result.getString(CurrencyParams.color))
                    val exchangeRate = result.getDouble(CurrencyParams.exchangeRate)

                    val currency = Currency(uuid, singular, plural)
                    currency.defaultBalance = defaultBalance
                    currency.symbol = symbol
                    currency.decimalSupported = decimalSupported
                    currency.defaultCurrency = defaultCurrency
                    currency.payable = payable
                    currency.color = color
                    currency.exchangeRate = exchangeRate

                    plugin.currencyManager.add(currency)
                    ServerUtils.log("Loaded currency: ${currency.plural}")
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    override fun updateCurrencyLocally(currency: Currency) {
        try {
            val connection = hikari.connection

            try {
                val statement = connection.prepareStatement(
                        SQLStatements.SELECT_CURRENCY_BY_ID(tablePrefix)
                )
                statement.setString(1, currency.uuid.toString())

                val result = statement.executeQuery()

                while (result.next()) {
                    val defaultBalance = result.getDouble(CurrencyParams.defaultBalance)
                    val symbol = result.getString(CurrencyParams.symbol)
                    val decimalSupported = result.getInt(CurrencyParams.decimalsSupported) == 1
                    val defaultCurrency = result.getInt(CurrencyParams.defaultCurrency) == 1
                    val payable = result.getInt(CurrencyParams.payable) == 1
                    val color = ChatColor.valueOf(result.getString(CurrencyParams.color))
                    val exchangeRate = result.getDouble(CurrencyParams.exchangeRate)

                    currency.defaultBalance = defaultBalance
                    currency.symbol = symbol
                    currency.decimalSupported = decimalSupported
                    currency.defaultCurrency = defaultCurrency
                    currency.payable = payable
                    currency.color = color
                    currency.exchangeRate = exchangeRate

                    ServerUtils.log("Updated currency: ${currency.plural}")
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    override fun saveCurrency(currency: Currency) {
        try {
            val connection = hikari.connection

            try {
                val statement = connection.prepareStatement(
                        SQLStatements.INSERT_CURRENCY(tablePrefix)
                )
                statement.setString(1, currency.uuid.toString())
                statement.setString(2, currency.singular)
                statement.setString(3, currency.plural)
                statement.setDouble(4, currency.defaultBalance)
                statement.setString(5, currency.symbol)
                statement.setInt(6, if (currency.decimalSupported) 1 else 0)
                statement.setInt(7, if (currency.defaultCurrency) 1 else 0)
                statement.setInt(8, if (currency.payable) 1 else 0)
                statement.setString(9, currency.color.name)
                statement.setDouble(10, currency.exchangeRate)
                statement.execute()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        plugin.updateForwarder.sendUpdateMessage(
            "currency",
            currency.uuid.toString()
        )
    }

    override fun deleteCurrency(currency: Currency) {
        try {
            val connection = hikari.connection

            try {
                val currenciesStatement = connection.prepareStatement(
                        SQLStatements.DELETE_CURRENCY_FROM_CURRENCIES(tablePrefix)
                )
                currenciesStatement.setString(1, currency.uuid.toString())
                currenciesStatement.execute()

                val balancesStatement = connection.prepareStatement(
                        SQLStatements.DELETE_BALANCES_WITH_CURRENCY_ID(tablePrefix)
                )
                balancesStatement.setString(1, currency.uuid.toString())
                balancesStatement.execute()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    override fun getTopList(currency: Currency, offset: Int, amount: Int): Map<String, Double> {
        if (currency.uuid in cachedTopLists) {
            val ctl = cachedTopLists[currency.uuid]
            if (ctl!!.matches(currency, offset, amount) && !ctl.expired) return ctl.results
        }

        val resultPair = linkedMapOf<String, Double>()
        try {
            val connection = hikari.connection

            try {
                val idBalance = linkedMapOf<String, Double>()
                val balanceStatement = connection.prepareStatement(
                        SQLStatements.SELECT_TOP_ACCOUNTS_BALANCES(tablePrefix, offset, amount)
                )
                balanceStatement.setString(1, currency.uuid.toString())

                val balanceResults = balanceStatement.executeQuery()
                while (balanceResults.next())
                    idBalance[balanceResults.getString(BalanceParams.accountID)] = balanceResults.getDouble(BalanceParams.balance)

                balanceResults.close()

                if (idBalance.isNotEmpty()) {
                    val accountStatement = connection.prepareStatement(
                            SQLStatements.SELECT_TOP_ACCOUNT_NICKNAMES(tablePrefix, idBalance.size)
                    )
                    var currentParam = 1
                    idBalance.keys.forEach {
                        accountStatement.setString(currentParam++, it)
                    }
                    val accountResults = accountStatement.executeQuery()
                    while (accountResults.next()) {
                        resultPair[accountResults.getString(AccountParams.nickname)] = idBalance[accountResults.getString(AccountParams.uuid)]!!
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        val ctl = CachedTopList(currency, amount, offset, System.currentTimeMillis())
        ctl.results = resultPair
        cachedTopLists[currency.uuid] = ctl
        return resultPair
    }

    override fun loadAccount(string: String): Account? = sharedLoadAccount(
        AccountParams.nickname,
        string
    )

    override fun loadAccount(uuid: UUID): Account? = sharedLoadAccount(
        AccountParams.uuid,
        uuid.toString()
    )

    override fun loadAllAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()

        try {
            val connection = hikari.connection
            try {
                val result = connection.prepareStatement(
                        SQLStatements.SELECT_ALL_ACCOUNT_IDS(tablePrefix)
                ).executeQuery()

                while (result.next()) {
                    sharedLoadAccount(AccountParams.uuid, result.getString(AccountParams.uuid))
                            ?.let {
                                accounts.add(it)
                            }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        return accounts
    }

    private fun sharedLoadAccount(idKey: String, id: String): Account? {
        var account: Account? = null

        try {
            val connection = hikari.connection
            try {
                val statement = connection.prepareStatement(
                        SQLStatements.SELECT_ACCOUNT(tablePrefix, idKey)
                )
                statement.setString(1, id)

                val result = statement.executeQuery()
                if (result.next()) {
                    account = Account(
                            UUID.fromString(result.getString(AccountParams.uuid)),
                            result.getString(AccountParams.nickname)
                    )
                    account.canReceiveCurrency = result.getInt(AccountParams.payable) == 1

                    val balancesStatement = connection.prepareStatement(
                            SQLStatements.SELECT_BALANCES_FOR_ACCOUNT(tablePrefix)
                    )
                    balancesStatement.setString(1, account.uuid.toString())

                    val balancesResult = balancesStatement.executeQuery()
                    while (balancesResult.next()) {
                        plugin.currencyManager.getCurrency(
                                UUID.fromString(balancesResult.getString(BalanceParams.currencyID))
                        )?.let {
                            account.modifyBalance(it, balancesResult.getDouble(BalanceParams.balance), false)
                        }
                    }
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        return account
    }

    override fun saveAccount(account: Account) {
        val (accountSQL, balances) = SQLStatements.INSERT_ACCOUNT_BALANCES(tablePrefix)

        try {
            val connection = hikari.connection

            try {
                val accountStatement = connection.prepareStatement(accountSQL)
                accountStatement.setString(1, account.displayName)
                accountStatement.setString(2, account.uuid.toString())
                accountStatement.setInt(3, if (account.canReceiveCurrency) 1 else 0)
                accountStatement.execute()

                plugin.currencyManager.currencies.forEach { currency ->
                    val balance = account.getBalance(currency.plural).let { if (it == -100.00) currency.defaultBalance else it }
                    val balanceStatement = connection.prepareStatement(balances)
                    balanceStatement.setString(1, account.uuid.toString())
                    balanceStatement.setString(2, currency.uuid.toString())
                    balanceStatement.setDouble(3, balance)
                    balanceStatement.execute()
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        plugin.updateForwarder.sendUpdateMessage("account", account.uuid.toString())
    }

    override fun deleteAccount(account: Account) {
        try {
            val connection = hikari.connection

            try {
                val accountStatement = connection.prepareStatement(
                        SQLStatements.DELETE_ACCOUNT(tablePrefix)
                )
                accountStatement.setString(1, account.uuid.toString())
                accountStatement.execute()

                val balanceStatement = connection.prepareStatement(
                        SQLStatements.DELETE_BALANCES_FOR_ACCOUNT(tablePrefix)
                )
                balanceStatement.setString(1, account.uuid.toString())
                balanceStatement.execute()
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    override fun createAccount(account: Account) {
        val (accountSQL, balances) = SQLStatements.INSERT_ACCOUNT_BALANCES(tablePrefix, true)

        try {
            val connection = hikari.connection
            try {
                val accountStatement = connection.prepareStatement(accountSQL)
                accountStatement.setString(1, account.displayName)
                accountStatement.setString(2, account.uuid.toString())
                accountStatement.setInt(3, if (account.canReceiveCurrency) 1 else 0)

                accountStatement.execute()

                plugin.currencyManager.currencies.forEach {
                    val balance = it.defaultBalance
                    val balanceStatement = connection.prepareStatement(balances)
                    balanceStatement.setString(1, account.uuid.toString())
                    balanceStatement.setString(2, it.uuid.toString())
                    balanceStatement.setDouble(3, balance)
                    balanceStatement.execute()
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                connection.close()
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }

        plugin.updateForwarder.sendUpdateMessage("account", account.uuid.toString())
    }
}
