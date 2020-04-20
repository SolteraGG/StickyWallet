package stickyWallet.utils

import org.bukkit.Bukkit
import org.bukkit.Server
import stickyWallet.utils.StringUtils.colorize

object ServerUtils {
    private val server: Server
        get() {
            return Bukkit.getServer()
        }

    private const val consolePrefix = "§2[StickyWallet] §f"
    private const val errorPrefix = "§2[StickyWallet Error] §f"

    fun log(message: String) = server.consoleSender.sendMessage("$consolePrefix${colorize(message)}")

    fun log(message: Throwable) = server.consoleSender.sendMessage("$errorPrefix$message")
}