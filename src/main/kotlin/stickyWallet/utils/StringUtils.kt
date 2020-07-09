package stickyWallet.utils

import com.google.common.collect.Lists
import java.lang.NumberFormatException
import java.text.DecimalFormat
import java.util.ArrayList
import kotlin.math.round
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import stickyWallet.files.L

object StringUtils {

    fun format(money: Double): String {
        val format = DecimalFormat()
        val symbols = format.decimalFormatSymbols

        symbols.groupingSeparator = ','
        symbols.decimalSeparator = '.'

        format.decimalFormatSymbols = symbols
        format.isGroupingUsed = true
        format.groupingSize = 3

        val roundOff = round(money * 100.0) / 100.0

        return format.format(roundOff)
    }

    fun colorize(string: String) = ChatColor.translateAlternateColorCodes('&', string)

    fun colorize(message: List<String>): ArrayList<String> {
        val colorizedList = Lists.newArrayList<String>()
        for (string in message) colorizedList.add(colorize(string))

        return colorizedList
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

    fun pluralize(amount: Int, singular: String, plural: String) = if (amount == 1) {
        singular
    } else {
        plural
    }
}
