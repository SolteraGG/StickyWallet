package stickyWallet.data

import StickyPlugin
import stickyWallet.accounts.Account
import stickyWallet.currency.Currency
import java.util.*

abstract class DataStore(val name: String, val isFetchingTopSupported: Boolean = false) {

    val plugin = StickyPlugin.instance

    abstract fun initialize(): Unit
    abstract fun close(): Unit

    abstract fun loadCurrencies(): Unit
    abstract fun updateCurrencyLocally(currency: Currency): Unit
    abstract fun saveCurrency(currency: Currency): Unit
    abstract fun deleteCurrency(currency: Currency): Unit

    abstract fun getTopList(currency: Currency, offset: Int, amount: Int): Map<String, Double>?

    abstract fun loadAccount(string: String): Account?
    abstract fun loadAccount(uuid: UUID): Account?
    abstract fun loadAllAccounts(): List<Account>
    abstract fun saveAccount(account: Account): Unit
    abstract fun deleteAccount(account: Account): Unit
    abstract fun createAccount(account: Account): Unit
}