package stickyWallet.currencies

import org.bukkit.ChatColor
import stickyWallet.utils.NumberUtilities
import java.math.BigDecimal
import java.util.UUID

data class Currency(
    var uuid: UUID,
    var singular: String,
    var plural: String,
    var type: String,

    var symbol: String? = null,
    var color: ChatColor = ChatColor.WHITE,
    var decimalSupported: Boolean = false,
    var defaultCurrency: Boolean = false,
    var defaultBalance: BigDecimal = BigDecimal.ZERO,
    var exchangeRate: BigDecimal = BigDecimal.ONE
) {
    fun format(amount: BigDecimal, compact: Boolean = false): String {
        val final = StringBuilder()

        this.symbol?.let { final.append(it) }

        if (compact || amount >= BigDecimal(10_000_000)) {
            final.append(NumberUtilities.compactFormat(amount))
        } else {
            val formatted = NumberUtilities.format(amount)

            if (this.decimalSupported) final.append(formatted)
            else final.append(formatted.split(".")[0])
        }

        final.append(" ")
            .append(if (amount == BigDecimal(1.0)) {
                this.singular.replace("_", " ")
            } else {
                this.plural.replace("_", " ")
            })

        return final.toString()
    }

    fun nameEquals(input: String) = singular.equals(input, true) || plural.equals(input, true)
}