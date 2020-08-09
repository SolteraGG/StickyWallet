package stickyWallet.events

import java.math.BigDecimal
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import stickyWallet.accounts.Account
import stickyWallet.currencies.Currency

class ConversionEvent(
    val exchanged: Currency,
    val received: Currency,
    val account: Account,
    val amountExchanged: BigDecimal,
    val amountReceived: BigDecimal
) : Event(), Cancellable {

    private val handlerList = HandlerList()
    private var cancel: Boolean = false

    override fun getHandlers() = handlerList

    override fun setCancelled(cancel1: Boolean) {
        cancel = cancel1
    }

    override fun isCancelled() = cancel

    val formattedReceivedAmount
        get() = received.format(amountReceived)

    val formattedExchangedAmount
        get() = exchanged.format(amountExchanged)
}
