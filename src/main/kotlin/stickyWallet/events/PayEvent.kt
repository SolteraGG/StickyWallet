package stickyWallet.events

import java.math.BigDecimal
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import stickyWallet.accounts.Account
import stickyWallet.currencies.Currency

class PayEvent(
    val currency: Currency,
    val payer: Account,
    val receiver: Account,
    val amount: BigDecimal
) : Event(), Cancellable {

    private val handlerList = HandlerList()
    private var cancel: Boolean = false

    override fun getHandlers() = handlerList

    override fun setCancelled(cancel1: Boolean) {
        cancel = cancel1
    }

    override fun isCancelled() = cancel

    val formattedAmount
        get() = currency.format(amount)
}
