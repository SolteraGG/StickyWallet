package stickyWallet.data

import stickyWallet.accounts.Account
import stickyWallet.currency.Currency
import org.bukkit.ChatColor
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import stickyWallet.utils.ServerUtils
import java.io.File
import java.io.IOException
import java.util.*

class YamlStorage(private val file: File) : DataStore("yaml", false) {

    private var config = YamlConfiguration()

    override fun initialize() {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) ServerUtils.log("Created yaml file successfully")
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
        try {
            config.load(file)
        } catch (ex: Exception) {
            when (ex) {
                is IOException, is InvalidConfigurationException -> ex.printStackTrace()
                else -> throw ex
            }
        }
    }

    override fun close() {}

    override fun loadCurrencies() {
        val section = config.getConfigurationSection("currencies")
        if (section != null) {
            val currencies = section.getKeys(false)
            for (uuid in currencies) {
                val currencyPath = "currencies.$uuid"
                val single = config.getString("$currencyPath.singular")!!
                val plural = config.getString("$currencyPath.plural")!!
                val currency = Currency(UUID.fromString(uuid), single, plural)
                currency.color = ChatColor.valueOf(config.getString("$currencyPath.color")!!.toUpperCase())
                currency.decimalSupported = config.getBoolean("$currencyPath.decimalSupported")
                currency.defaultBalance = config.getDouble("$currencyPath.defaultBalance")
                currency.defaultCurrency = config.getBoolean("$currencyPath.defaultCurrency")
                currency.payable = config.getBoolean("$currencyPath.payable")
                currency.symbol = config.getString("$currencyPath.symbol")
                currency.exchangeRate = config.getDouble("$currencyPath.exchange_rate")

                plugin.currencyManager.add(currency)
                ServerUtils.log("Loaded currency: ${currency.singular}")
            }
        }
    }

    override fun updateCurrencyLocally(currency: Currency) {
        throw UnsupportedOperationException("This method is not supported in yaml!")
    }

    override fun saveCurrency(currency: Currency) {
        val path = "currencies.${currency.uuid}"
        config.set("$path.singular", currency.singular)
        config.set("$path.plural", currency.plural)
        config.set("$path.defaultBalance", currency.defaultBalance)
        config.set("$path.symbol", currency.symbol)
        config.set("$path.decimalSupported", currency.decimalSupported)
        config.set("$path.defaultCurrency", currency.defaultCurrency)
        config.set("$path.payable", currency.payable)
        config.set("$path.color", currency.color.name)
        config.set("$path.exchange_rate", currency.exchangeRate)

        try {
            config.save(file)
        } catch (ex: IOException) {
            ServerUtils.log("Failed to save currencies file!")
            ex.printStackTrace()
        }
    }

    override fun deleteCurrency(currency: Currency) {
        val path = "currencies.${currency.uuid}"
        config.set(path, null)
        try {
            config.save(file)
        } catch (ex: IOException) {
            ServerUtils.log("Failed to save currencies file!")
            ex.printStackTrace()
        }
    }

    override fun getTopList(currency: Currency, offset: Int, amount: Int): Map<String, Double>? = null

    override fun loadAccount(string: String): Account? {
        val section = config.getConfigurationSection("accounts")
        if (section != null) {
            val accounts = section.getKeys(false)
            if (accounts.isNotEmpty()) {
                accounts.forEach {
                    val path = "accounts.$it"
                    val nickname = config.getString("$path.nickname")
                    if (nickname != null && nickname.equals(string, true)) {
                        val account = Account(UUID.fromString(it), nickname)
                        account.canReceiveCurrency = config.getBoolean("$path.payable")
                        loadBalances(account)
                        plugin.accountManager.addAccount(account)
                        return account
                    }
                }
            }
        }
        return null
    }

    override fun loadAccount(uuid: UUID): Account? {
        val path = "accounts.$uuid"
        val nickname = config.getString("$path.nickname")
        if (nickname != null) {
            val account = Account(uuid, nickname)
            account.canReceiveCurrency = config.getBoolean("$path.payable")
            loadBalances(account)
            plugin.accountManager.addAccount(account)
            return account
        }
        return null
    }

    override fun saveAccount(account: Account) {
        val path = "accounts.${account.uuid}"
        config.set("$path.nickname", account.nickname)
        config.set("$path.uuid", account.uuid.toString())
        config.set("$path.payable", account.canReceiveCurrency)
        account.currencyBalances.forEach { (currency, amount) ->
            config.set("$path.balances.${currency.uuid}", amount)
        }
        try {
            config.save(file)
        } catch (ex: IOException) {
            ServerUtils.log("Failed to save currencies file!")
            ex.printStackTrace()
        }
    }

    override fun deleteAccount(account: Account) {
        val path = "accounts.${account.uuid}"
        config.set(path, null)
        try {
            config.save(file)
        } catch (ex: IOException) {
            ServerUtils.log("Failed to save currencies file!")
            ex.printStackTrace()
        }
    }

    override fun createAccount(account: Account) {}

    override fun loadAllAccounts(): List<Account> {
        val accounts = mutableListOf<Account>()
        config.getConfigurationSection("accounts")!!.getKeys(false)
            .forEach {
                val acc = loadAccount(UUID.fromString(it))
                if (acc != null) {
                    loadBalances(acc)
                    accounts.add(acc)
                }
            }
        return accounts
    }

    private fun loadBalances(account: Account) {
        val path = "accounts.${account.uuid}"
        val balanceSection = config.getConfigurationSection("$path.balances")
        if (balanceSection != null) {
            val balances = balanceSection.getKeys(false)
            if (balances.isNotEmpty()) {
                balances.forEach {
                    val path2 = "$path.balances.$it"
                    val balance = config.getDouble(path2)
                    val currency = plugin.currencyManager.getCurrency(UUID.fromString(it))
                    if (currency != null) account.modifyBalance(currency, balance, false)
                }
            }
        }
    }

}