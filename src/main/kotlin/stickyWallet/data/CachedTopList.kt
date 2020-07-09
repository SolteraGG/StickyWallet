package stickyWallet.data

import java.util.concurrent.TimeUnit
import stickyWallet.currency.Currency

data class CachedTopList(
    val currency: Currency,
    val amount: Int,
    val offset: Int,
    val cacheTime: Long
) {

    var results = linkedMapOf<String, Double>()

    fun matches(currency: Currency, offset: Int, amount: Int) =
            currency.uuid == this.currency.uuid &&
            offset == this.offset &&
            amount == this.amount

    val expired
        get() = System.currentTimeMillis() - this.cacheTime > TimeUnit.MINUTES.toMillis(1)
}
