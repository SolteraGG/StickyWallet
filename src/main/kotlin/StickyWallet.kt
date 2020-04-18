import org.bukkit.plugin.java.JavaPlugin

import currency.CurrencyManager
import files.Configuration
import utils.ServerUtils
import vault.VaultManager

class StickyWallet : JavaPlugin() {

    lateinit var currencyManager: CurrencyManager
    private lateinit var vaultManager: VaultManager

    var debug = false;
    private var vaultSupport = false
    var loggingTransactions = false

    var isDisabling = false;

    override fun onLoad() {
        this.logger.info("Loading ${this.name}")

        val configuration = Configuration(this)
        configuration.loadDefaultConfig()

        this.debug = this.config.getBoolean("debug")
        this.vaultSupport = this.config.getBoolean("vault")
        this.loggingTransactions = this.config.getBoolean("transaction_log")
    }

    override fun onEnable() {
        instance = this;

        currencyManager = CurrencyManager(this)

        if (this.vaultSupport) {
            this.vaultManager = VaultManager(this)
            vaultManager.hook()
        } else {
            ServerUtils.log("Vault linking has been enabled")
        }
    }

    override fun onDisable() {
        isDisabling = true

        if (this.vaultSupport) this.vaultManager.unhook()
    }

    companion object {
        lateinit var instance: StickyWallet

        fun doAsync(runnable: Runnable) {
            this.instance.server.scheduler.runTaskAsynchronously(instance, runnable)
        }

        fun doSync(runnable: Runnable) {
            this.instance.server.scheduler.runTask(instance, runnable)
        }
    }

}