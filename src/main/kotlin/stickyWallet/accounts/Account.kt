package stickyWallet.accounts

import StickyPlugin
import stickyWallet.currency.Currency
import stickyWallet.events.ConversionEvent
import stickyWallet.events.TransactionEvent
import org.bukkit.Bukkit
import stickyWallet.utils.ServerUtils
import stickyWallet.utils.TransactionType
import java.util.*
import kotlin.math.round

data class Account(val uuid: UUID, var nickname: String?) {

    val currencyBalances = mutableMapOf<Currency, Double>()
    var canReceiveCurrency = true

    val displayName
        get() = nickname ?: uuid.toString()

    fun withdraw(currency: Currency, amount: Double): Boolean {
        if (!hasEnough(currency, amount)) return false

        val event = TransactionEvent(currency, this, amount, TransactionType.WITHDRAW)
        StickyPlugin.doSync(Runnable {
            Bukkit.getPluginManager().callEvent(event)
        })
        if (event.isCancelled) return false

        val finalAmount = getBalance(currency) - amount
        modifyBalance(currency, finalAmount, true)
        StickyPlugin.instance.economyLogger.log("""
            [WITHDRAW] Account: $displayName
            Withdrawn: ${currency.format(amount)}
            Total: ${currency.format(finalAmount)}
        """.trimIndent())
        return true
    }

    fun deposit(currency: Currency, amount: Double): Boolean {
        if (!canReceiveCurrency) return false

        val event = TransactionEvent(currency, this, amount, TransactionType.DEPOSIT)
        StickyPlugin.doSync(Runnable {
            Bukkit.getPluginManager().callEvent(event)
        })
        if (event.isCancelled) return false

        val finalAmount = getBalance(currency) + amount
        modifyBalance(currency, finalAmount, true)
        StickyPlugin.instance.economyLogger.log("""
            [DEPOSIT] Account: $displayName
            Deposited: ${currency.format(amount)}
            Total: ${currency.format(finalAmount)}
        """.trimIndent())
        return true
    }

    fun convert(exchanged: Currency, exchangeAmount: Double, received: Currency, amount: Double): Boolean {
        val event = ConversionEvent(exchanged, received, this, exchangeAmount, amount)
        StickyPlugin.doSync(Runnable {
            Bukkit.getPluginManager().callEvent(event)
        })
        if (event.isCancelled) return false

        if (amount != -1.0) {
            val removed = getBalance(exchanged) - exchangeAmount
            val added = getBalance(received) + amount
            modifyBalance(exchanged, removed, false)
            modifyBalance(received, added, false)
            StickyPlugin.instance.dataStore.saveAccount(this)
            StickyPlugin.instance.economyLogger.log("""
                [CONVERSION - Custom Amount] Account: $displayName
                Converted ${exchanged.format(exchangeAmount)} to ${received.format(amount)}
            """.trimIndent())
            return true
        }

        var receiveRate = false
        val rate = if (exchanged.exchangeRate > received.exchangeRate) {
            exchanged.exchangeRate
        } else {
            receiveRate = true
            received.exchangeRate
        }

        val finalAmount = round(exchangeAmount * rate)
        val removed = getBalance(exchanged) - if (!receiveRate) {
            exchangeAmount
        } else {
            finalAmount
        }
        val added = getBalance(received) + if (!receiveRate) {
            finalAmount
        } else {
            exchangeAmount
        }

        if (!hasEnough(exchanged, if (!receiveRate) exchangeAmount else finalAmount))
            return false;

        if (StickyPlugin.instance.debug) {
            ServerUtils.log("""
                Rate: $rate
                Final Amount: $finalAmount
                Removed amount: ${exchanged.format(removed)}
                Added amount: ${received.format(added)}
            """.trimIndent())
        }

        modifyBalance(exchanged, removed, false)
        modifyBalance(received, added, false)
        StickyPlugin.instance.dataStore.saveAccount(this)
        StickyPlugin.instance.economyLogger.log("""
            [CONVERSION - Preset Rate] Account: $displayName
            Converted ${exchanged.format(exchangeAmount)} (rate: $rate) to ${received.format(amount)}
        """.trimIndent())

        return true
    }

    fun setBalance(currency: Currency, amount: Double) {
        val event = TransactionEvent(currency, this, amount, TransactionType.SET)
        StickyPlugin.doSync(Runnable {
            Bukkit.getPluginManager().callEvent(event)
        })
        if (event.isCancelled) return

        StickyPlugin.instance.economyLogger.log("""
            [BALANCE SET] Account: $displayName
            Total: ${currency.format(amount)}
        """.trimIndent())
        modifyBalance(currency, amount, true)
    }

    fun hasEnough(currency: Currency, amount: Double) = getBalance(currency) >= amount
    fun hasEnough(amount: Double) = StickyPlugin.instance.currencyManager.getDefaultCurrency()?.let { hasEnough(it, amount) } ?: false

    fun getBalance(identifier: String): Double =
            currencyBalances.entries
                    .find { (currency) ->
                        currency.singular.equals(identifier, true) || currency.plural.equals(identifier, true)
                    }?.component2() ?: -100.0

    fun getBalance(currency: Currency): Double {
        if (currency in currencyBalances) return currencyBalances[currency]!!
        return currency.defaultBalance
    }

    fun modifyBalance(currency: Currency, amount: Double, save: Boolean = false) {
        currencyBalances[currency] = amount
        if (save)
            StickyPlugin.instance.dataStore.saveAccount(this)
    }

}