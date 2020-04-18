package currency

import StickyWallet
import com.google.common.collect.Lists
import java.util.*

class CurrencyManager(val plugin: StickyWallet) {

    val currencies: ArrayList<Currency> = Lists.newArrayList()

    fun currencyExists(name: String) = this.currencies.any { it.singular.equals(name, true) || it.plural.equals(name, true) }

    fun getCurrency(name: String) = this.currencies.find { it.singular.equals(name, true) || it.plural.equals(name, true) }

    fun getCurrency(uuid: UUID) = this.currencies.find { it.uuid == uuid }

    fun getDefaultCurrency(): Currency? = this.currencies.first { it.defaultCurrency }

    fun createNewCurrency(singular: String, plural: String) {
        if (currencyExists(singular) || currencyExists(plural)) {
            return
        }

        val currency = Currency(UUID.randomUUID(), singular, plural)
        currency.exchangeRate = 1.0

        if (currencies.isEmpty()) currency.defaultCurrency = true

        this.add(currency)
        // TODO: plugin.dataStore.saveCurrency(currency)
    }

    private fun add(currency: Currency) {
        if (this.currencies.contains(currency)) return
        this.currencies.add(currency)
    }
}