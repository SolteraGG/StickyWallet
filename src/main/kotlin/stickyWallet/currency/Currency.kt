package stickyWallet.currency

import java.lang.StringBuilder
import java.text.NumberFormat
import java.util.UUID
import org.bukkit.ChatColor
import stickyWallet.utils.StringUtils

data class Currency(var uuid: UUID, var singular: String, var plural: String) {

    var symbol: String? = null
    var color = ChatColor.WHITE
    var decimalSupported = false
    var payable = true
    var defaultCurrency = false
    var defaultBalance = 0.0
    var exchangeRate = 0.0

    fun format(amount: Double): String {
        val amt = StringBuilder()

        if (this.symbol != null) amt.append(this.symbol)
        if (this.decimalSupported) {
            amt.append(StringUtils.format(amount))
        } else {
            var s = amount.toString()
            val splitString = s.split(".")
            if (splitString.isNotEmpty()) s = splitString[0]
            amt.append(NumberFormat.getInstance().format(s.toDouble()))
        }
        amt.append(" ")
        amt.append(if (amount == 1.0) {
            this.singular.replace("_", " ")
        } else {
            this.plural.replace("_", " ")
        })
        return amt.toString()
    }
}
