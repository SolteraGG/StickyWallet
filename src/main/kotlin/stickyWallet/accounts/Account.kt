package stickyWallet.accounts

import org.bukkit.Bukkit
import stickyWallet.StickyWallet
import stickyWallet.configs.PluginConfiguration
import stickyWallet.currencies.Currency
import stickyWallet.events.ConversionEvent
import stickyWallet.events.TransactionEvent
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.TransactionType
import java.math.BigDecimal
import java.util.UUID

open class Account(
    val uuid: UUID,
    var playerName: String?
) : UsePlugin {
    val balances = mutableMapOf<Currency, BigDecimal>()

    val displayName
        get() = playerName ?: uuid.toString()

    fun getBalanceForCurrency(name: String) = balances.entries
        .find { (currency) -> currency.nameEquals(name) }
        ?.component2() ?: 0.0

    fun getBalanceForCurrency(currency: Currency) = if (currency in balances) {
        balances[currency]!!
    } else {
        currency.defaultBalance
    }

    open fun hasEnough(currency: Currency, amount: BigDecimal) = getBalanceForCurrency(currency) >= amount
    open fun hasEnough(amount: BigDecimal) = pluginInstance.currencyStore.getDefaultCurrency()
        ?.let { hasEnough(it, amount) } ?: false

    fun modifyCurrencyBalance(currency: Currency, amount: BigDecimal, save: Boolean = false) {
        balances[currency] = amount

        if (save) {
            pluginInstance.dataHandler.saveAccount(this)
        }
    }

    open fun withdraw(currency: Currency, amount: BigDecimal): Boolean {
        if (!hasEnough(currency, amount)) return false

        val event = TransactionEvent(
            currency,
            this,
            amount,
            TransactionType.SET
        )

        StickyWallet.doSync {
            Bukkit.getPluginManager().callEvent(event)
        }

        if (event.isCancelled) return false

        val finalAmount = getBalanceForCurrency(currency) - amount
        modifyCurrencyBalance(currency, finalAmount, save = true)

        pluginInstance.economyLogger.info("""
            [WITHDRAW] Account: $displayName
              Withdrawn: ${currency.format(amount)}
              Total: ${currency.format(finalAmount)}
        """.trimIndent())
        return true
    }

    fun deposit(currency: Currency, amount: BigDecimal): Boolean {
        val event = TransactionEvent(
            currency,
            this,
            amount,
            TransactionType.DEPOSIT
        )

        StickyWallet.doSync {
            Bukkit.getPluginManager().callEvent(event)
        }

        if (event.isCancelled) return false

        val finalAmount = getBalanceForCurrency(currency) + amount
        modifyCurrencyBalance(currency, finalAmount, save = true)
        pluginInstance.economyLogger.info("""
            [DEPOSIT] Account: $displayName
              Deposited: ${currency.format(amount)}
              Total: ${currency.format(finalAmount)}       
        """.trimIndent())
        return true
    }

    fun set(currency: Currency, amount: BigDecimal): Boolean {
        val event = TransactionEvent(
            currency,
            this,
            amount,
            TransactionType.SET
        )

        StickyWallet.doSync {
            Bukkit.getPluginManager().callEvent(event)
        }

        if (event.isCancelled) return false

        modifyCurrencyBalance(currency, amount, save = true)
        pluginInstance.economyLogger.info("""
            [BALANCE SET] Account: $displayName
              New Balance: ${currency.format(amount)}
        """.trimIndent())

        return true
    }

    fun convert(exchanged: Currency, exchangeAmount: BigDecimal, received: Currency, amount: BigDecimal): Boolean {
        val event = ConversionEvent(exchanged, received, this, exchangeAmount, amount)

        StickyWallet.doSync {
            Bukkit.getPluginManager().callEvent(event)
        }

        if (event.isCancelled) return false

        if (amount != BigDecimal.ONE.negate()) {
            val removed = getBalanceForCurrency(exchanged) - exchangeAmount
            val added = getBalanceForCurrency(received) + amount
            modifyCurrencyBalance(exchanged, removed, save = false)
            modifyCurrencyBalance(received, added, save = false)
            pluginInstance.dataHandler.saveAccount(this)
            pluginInstance.economyLogger.info("""
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

        val finalAmount = exchangeAmount.multiply(rate)
        val removed = getBalanceForCurrency(exchanged) - if (!receiveRate) {
            exchangeAmount
        } else {
            finalAmount
        }
        val added = getBalanceForCurrency(received) + if (!receiveRate) {
            finalAmount
        } else {
            exchangeAmount
        }

        if (!hasEnough(exchanged, if (!receiveRate) exchangeAmount else finalAmount))
            return false

        if (PluginConfiguration.DebugSettings.logsEnabled) {
            pluginInstance.logger.info("""
                Rate: $rate
                Final Amount: $finalAmount
                Removed amount: ${exchanged.format(removed)}
                Added amount: ${received.format(added)}
            """.trimIndent())
        }

        modifyCurrencyBalance(exchanged, removed, false)
        modifyCurrencyBalance(received, added, false)
        pluginInstance.dataHandler.saveAccount(this)
        pluginInstance.economyLogger.info("""
            [CONVERSION - Preset Rate] Account: $displayName
            Converted ${exchanged.format(exchangeAmount)} (rate: $rate) to ${received.format(amount)}
        """.trimIndent())

        return true
    }
}
