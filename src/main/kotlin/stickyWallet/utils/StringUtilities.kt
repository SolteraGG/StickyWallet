package stickyWallet.utils

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import stickyWallet.configs.L

object StringUtilities {
    /**
     * Parses color codes from a string (specified with &)
     */
    fun colorize(string: String) = ChatColor.translateAlternateColorCodes('&', string)

    /**
     * Parses color codes from all strings in the list (specified with &),
     * joining the end result in one big string
     */
    fun colorize(messages: List<String>) = messages.map { colorize(it) }

    /**
     * Simple utility to pluralize a string based on a number
     */
    fun pluralize(amount: Int, singular: String, plural: String) = if (amount == 1) {
        singular
    } else {
        plural
    }

    fun validateInput(sender: CommandSender, input: String): Boolean {
        return try {
            val amount = input.toDouble()
            if (amount < 0) throw NumberFormatException()
            true
        } catch (ex: NumberFormatException) {
            sender.sendMessage("${L.prefix}${L.invalidAmount}")
            false
        }
    }
}
