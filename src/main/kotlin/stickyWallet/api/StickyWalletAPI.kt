package stickyWallet.api

import stickyWallet.currency.Currency
import java.util.*

class StickyWalletAPI {

    private val plugin = StickyPlugin.instance;

    init {
        if (plugin.currencyManager.getDefaultCurrency() == null) {
            plugin.logger.warning("""
                ||
                ||
                ||
                There is no default currency set, the API **WILL NOT WORK**!
                ||
                ||
                ||
            """.trimIndent())
        }
    }

    fun deposit(uuid: UUID, amount: Double) {
        val acc = plugin.accountManager.getAccount(uuid)
        plugin.currencyManager.getDefaultCurrency()?.let { acc?.deposit(it, amount) }
    }

    fun deposit(uuid: UUID, amount: Double, currency: Currency) {
        val acc = plugin.accountManager.getAccount(uuid)
        acc?.deposit(currency, amount)
    }

    fun withdraw(uuid: UUID, amount: Double) {
        val acc = plugin.accountManager.getAccount(uuid)
        plugin.currencyManager.getDefaultCurrency()?.let { acc?.withdraw(it, amount) }
    }

    fun withdraw(uuid: UUID, amount: Double, currency: Currency) {
        val acc = plugin.accountManager.getAccount(uuid)
        acc?.withdraw(currency, amount)
    }

    fun getBalance(uuid: UUID): Double? {
        val acc = plugin.accountManager.getAccount(uuid)
        return plugin.currencyManager.getDefaultCurrency()?.let { acc?.getBalance(it) }
    }

    fun getBalance(uuid: UUID, currency: Currency): Double? = plugin.accountManager.getAccount(uuid)?.getBalance(currency)

    fun getCurrency(name: String): Currency? = this.plugin.currencyManager.getCurrency(name)

}