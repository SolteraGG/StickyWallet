package stickyWallet.currencies

import org.bukkit.ChatColor
import stickyWallet.utils.NumberUtilities
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
    var defaultBalance: Double = 0.0,
    var exchangeRate: Double = 1.0
) {
    fun format(amount: Double, compact: Boolean = false): String {
        val final = StringBuilder()

        this.symbol?.let { final.append(it) }

        if (this.decimalSupported) {
            final.append(NumberUtilities.format(amount))
        } else {
            final.append(
                if (compact) {
                    NumberUtilities.compactFormat(amount)
                } else {
                    NumberUtilities.format(amount).split(".")[0]
                }
            )
        }

        final.append(" ")
            .append(if (amount == 1.0) {
                this.singular.replace("_", " ")
            } else {
                this.plural.replace("_", " ")
            })

        return final.toString()
    }

    fun nameEquals(input: String) = singular.equals(input, true) || plural.equals(input, true)
}