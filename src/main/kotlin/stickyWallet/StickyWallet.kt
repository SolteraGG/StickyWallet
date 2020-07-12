package stickyWallet

import org.bukkit.plugin.java.JavaPlugin
import stickyWallet.accounts.AccountStore
import stickyWallet.apis.PlaceholderAPI
import stickyWallet.bungee.UpdateForwarder
import stickyWallet.commands.BalanceCommand
import stickyWallet.commands.BalanceTopCommand
import stickyWallet.commands.CheckCommand
import stickyWallet.commands.CurrencyCommand
import stickyWallet.commands.EconomyCommand
import stickyWallet.commands.PayCommand
import stickyWallet.configs.PluginConfiguration
import stickyWallet.currencies.CurrencyStore
import stickyWallet.interfaces.DataHandler
import stickyWallet.listeners.CraftEvent
import stickyWallet.listeners.JoinLeaveListeners
import stickyWallet.sql.PostgresHandler
import stickyWallet.utils.EconomyLogger
import stickyWallet.utils.StickyConsole
import stickyWallet.vault.VaultManager

class StickyWallet : JavaPlugin() {
    val logger: StickyConsole
        get() = StickyConsole

    val economyLogger: EconomyLogger
        get() = EconomyLogger

    val accountStore = AccountStore
    val currencyStore = CurrencyStore
    lateinit var dataHandler: DataHandler

    var isDisabling = false

    override fun onLoad() {
        logger.info("Loading plugin $name, version ${description.version}")
        instance = this

        PluginConfiguration.initialize()
    }

    override fun onEnable() {
        logger.info("Enabling plugin $name: loading everything")

        dataHandler = PostgresHandler

        try {
            logger.info("Initializing data store \"${dataHandler.name}\"...")
            dataHandler.initialize()
            dataHandler.loadCurrencies()
            logger.info("Successfully loaded \"${dataHandler.name}\" with ${currencyStore.currencies.size} currencies")
        } catch (ex: Exception) {
            logger.error("§cCannot load initial data from the data store!")
            logger.error("§cDouble check your config, and try again!")
            ex.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(JoinLeaveListeners(), this)
        server.pluginManager.registerEvents(CraftEvent(), this)

        val balCommand = getCommand("balance")!!
        balCommand.setExecutor(BalanceCommand())
        balCommand.usage = "[player]"

        val checkCommand = getCommand("check")!!
        checkCommand.setExecutor(CheckCommand())
        checkCommand.usage = "<redeem|write> [amount] [currency]"

        val currencyCommand = getCommand("currency")!!
        currencyCommand.setExecutor(CurrencyCommand())
        currencyCommand.usage =
            "<backend|color|colorlist|convert|create|decimals|default|delete|list|payable|setrate|startbal|view>"

        val economyCommand = getCommand("economy")!!
        economyCommand.setExecutor(EconomyCommand())
        economyCommand.usage = "<add|give|remove|set|take> <account> <amount> [currency]"

        val payCommand = getCommand("pay")!!
        payCommand.setExecutor(PayCommand())
        payCommand.usage = "<account> <amount> [currency]"

        val balTopCommand = getCommand("baltop")!!
        balTopCommand.setExecutor(BalanceTopCommand())
        balTopCommand.usage = "[currency] [page]"

        // Register PAPI support if present
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            logger.info("Registering PlaceholderAPI placeholders")
            PlaceholderAPI().register()
        }

        if (PluginConfiguration.IntegrationSettings.vaultEnabled) {
            VaultManager.hook()
        }

        if (PluginConfiguration.DebugSettings.transactionLogs) economyLogger.save()

        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        server.messenger.registerIncomingPluginChannel(this, "BungeeCord", UpdateForwarder)
    }

    override fun onDisable() {
        logger.info("No more StickyWallet...")

        isDisabling = true

        if (PluginConfiguration.IntegrationSettings.vaultEnabled) {
            VaultManager.unhook()
        }
    }

    companion object {
        lateinit var instance: StickyWallet

        fun doAsync(runnable: () -> Unit) {
            instance.server.scheduler.runTaskAsynchronously(instance, runnable)
        }

        fun doSync(runnable: () -> Unit) {
            instance.server.scheduler.runTask(instance, runnable)
        }

        fun doLater(after: Long, runnable: () -> Unit) {
            instance.server.scheduler.runTaskLater(instance, runnable, after)
        }
    }
}
