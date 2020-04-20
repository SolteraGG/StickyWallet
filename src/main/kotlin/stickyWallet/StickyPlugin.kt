import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import stickyWallet.accounts.AccountManager
import stickyWallet.check.CheckManager
import stickyWallet.commands.BalanceCommand
import stickyWallet.commands.CheckCommand
import stickyWallet.commands.CurrencyCommand
import stickyWallet.currency.CurrencyManager
import stickyWallet.data.DataStore
import stickyWallet.data.DataStoreManager
import stickyWallet.files.Configuration
import stickyWallet.listeners.StickyListener
import stickyWallet.nbt.NMSManager
import stickyWallet.utils.EconomyLogger
import stickyWallet.utils.ServerUtils
import stickyWallet.vault.VaultManager

class StickyPlugin : JavaPlugin() {

    lateinit var dataStoreManager: DataStoreManager
    lateinit var dataStore: DataStore
    lateinit var accountManager: AccountManager
    lateinit var currencyManager: CurrencyManager
    lateinit var checkManager: CheckManager
    private lateinit var vaultManager: VaultManager
    lateinit var nmsManager: NMSManager
    lateinit var economyLogger: EconomyLogger

    var debug = false
    private var vaultSupport = false
    var loggingTransactions = false

    var isDisabling = false

    override fun onLoad() {
        logger.info("Loading $name")

        val configuration = Configuration(this)
        configuration.loadDefaultConfig()

        debug = config.getBoolean("debug")
        vaultSupport = config.getBoolean("vault")
        loggingTransactions = config.getBoolean("transaction_log")
    }

    override fun onEnable() {
        instance = this

        nmsManager = NMSManager()
        accountManager = AccountManager(this)
        currencyManager = CurrencyManager(this)
        checkManager = CheckManager(this)
        economyLogger = EconomyLogger(this)

        dataStoreManager = DataStoreManager()

        initializeDataStore(config.getString("storage", "yaml"), true)

        server.pluginManager.registerEvents(StickyListener(), this)

        val balCommand = getCommand("balance")!!
        balCommand.setExecutor(BalanceCommand())
        balCommand.usage = "[player]"

        val checkCommand = getCommand("check")!!
        checkCommand.setExecutor(CheckCommand())
        checkCommand.usage = "<redeem|write> [amount] [currency]"

        val currencyCommand = getCommand("currency")!!
        currencyCommand.setExecutor(CurrencyCommand())
        currencyCommand.usage = "<backend|color|colorlist|convert|create|decimals|default|delete|list|payable|setrate|startbal|view>"

        if (vaultSupport) {
            vaultManager = VaultManager(this)
            vaultManager.hook()
        } else {
            ServerUtils.log("Vault linking has been enabled")
        }

        if (loggingTransactions) economyLogger.save()
    }

    override fun onDisable() {
        isDisabling = true

        if (vaultSupport) vaultManager.unhook()

        dataStore.close()
    }

    fun initializeDataStore(type: String?, fetch: Boolean) {
        val store: DataStore?

        if (type != null) {
            store = dataStoreManager.getStore(type)
        } else {
            ServerUtils.log("§cNo valid storage method provided.")
            ServerUtils.log("§cCheck your config file, then try again!")
            ServerUtils.log("§cValid storage methods are: ${ChatColor.RED}yaml §cor ${ChatColor.RED}mysql")
            server.pluginManager.disablePlugin(this)
            return
        }

        if (store == null) {
            ServerUtils.log("§cNo valid storage method provided.")
            ServerUtils.log("§cCheck your config file, then try again!")
            ServerUtils.log("§cValid storage methods are: ${ChatColor.RED}yaml §cor ${ChatColor.RED}mysql")
            server.pluginManager.disablePlugin(this)
            return
        }

        dataStore = store
        try {
            ServerUtils.log("Initializing data store \"${dataStore.name}\"...")
            dataStore.initialize()

            if (fetch) {
                ServerUtils.log("Loading currencies...")
                dataStore.loadCurrencies()
                ServerUtils.log("Successfully loaded ${currencyManager.currencies.size} currencies!")
            }
        } catch (ex: Throwable) {
            ServerUtils.log("§cCannot load initial data from the data store!")
            ServerUtils.log("§cDouble check your config, and try again!")
            ex.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    companion object {
        lateinit var instance: StickyPlugin

        fun doAsync(runnable: Runnable) {
            instance.server.scheduler.runTaskAsynchronously(instance, runnable)
        }

        fun doSync(runnable: Runnable) {
            instance.server.scheduler.runTask(instance, runnable)
        }
    }

}