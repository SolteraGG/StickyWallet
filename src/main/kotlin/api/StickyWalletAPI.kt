package api

import currency.Currency
import java.util.*

class StickyWalletAPI {

    var plugin = StickyWallet.instance;

    init {
        // TODO:
        if (true) {
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
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.deposit(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun deposit(uuid: UUID, amount: Double, currency: Any) {
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.deposit(currency ?? plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun withdraw(uuid: UUID, amount: Double) {
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.withdraw(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun withdraw(uuid: UUID, amount: Double, currency: Any) {
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.withdraw(currency ?? plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun getBalance(uuid: UUID, amount: Double) {
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.getBalance(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun getBalance(uuid: UUID, amount: Double, currency: Any) {
        // TODO
        // Account acc = plugin.getAccountManager().getAccount(uuid);
        // acc.getBalance(currency ?? plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    fun getCurrency(name: String): Currency? {
        return this.plugin.currencyManager.getCurrency(name)
    }

}