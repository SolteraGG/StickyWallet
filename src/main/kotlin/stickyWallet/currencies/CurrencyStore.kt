package stickyWallet.currencies

import stickyWallet.interfaces.UsePlugin
import java.util.UUID

object CurrencyStore : UsePlugin {
    val currencies = mutableListOf<Currency>()

    fun currencyExists(name: String) = currencies.any { it.singular.equals(name, true) || it.plural.equals(name, true) }

    fun getCurrency(name: String) = currencies.find { it.singular.equals(name, true) || it.plural.equals(name, true) }
    fun getCurrency(uuid: UUID) = currencies.find { it.uuid == uuid }

    fun getDefaultCurrency(): Currency? = currencies.firstOrNull { it.defaultCurrency }

    fun createNewCurrency(
        singular: String,
        plural: String,
        type: String
    ): Boolean {
        val existent = getCurrency(singular) ?: getCurrency(plural)

        if (existent != null) return false

        val currency = Currency(UUID.randomUUID(), singular, plural, type)

        if (currencies.isEmpty()) currency.defaultCurrency = true

        if (!currencies.contains(currency)) currencies.add(currency)

        pluginInstance.dataHandler.saveCurrency(currency)

        return true
    }

    fun addCachedCurrency(currency: Currency) = if (currencies.contains(currency)) {
        false
    } else {
        currencies.add(currency)
    }
}
