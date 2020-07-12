package stickyWallet.interfaces

import stickyWallet.accounts.Account
import stickyWallet.currencies.Currency
import java.math.BigDecimal
import java.util.UUID

abstract class DataHandler(val name: String) : UsePlugin {
    abstract fun initialize(): Unit

    abstract fun getTopList(currency: Currency, offset: Long, amount: Int): Map<String, BigDecimal>?

    abstract fun loadCurrencies(): Unit
    abstract fun saveCurrency(currency: Currency): Unit
    abstract fun deleteCurrency(currency: Currency): Unit
    abstract fun updateCachedCurrency(currency: Currency): Unit

    abstract fun loadAccount(playerName: String): Account?
    abstract fun loadAccount(uuid: UUID): Account?
    abstract fun saveAccount(account: Account): Unit
    abstract fun deleteAccount(account: Account): Unit
}