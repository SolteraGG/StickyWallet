package stickyWallet.utils

import org.bukkit.Bukkit
import stickyWallet.configs.PluginConfiguration
import stickyWallet.utils.Constants.consolePrefix
import stickyWallet.utils.Constants.errorPrefix
import stickyWallet.utils.StringUtilities.colorize

object StickyConsole {
    private val server = Bukkit.getServer()

    fun info(message: String) = server.consoleSender.sendMessage(consolePrefix + colorize(message))

    fun error(message: String) = server.consoleSender.sendMessage(errorPrefix + colorize(message))
    fun error(error: Exception) = error(error.toString())

    fun logIfTransactionLogEnabled(message: String) {
        if (PluginConfiguration.DebugSettings.transactionLogs) info(message)
    }
}
