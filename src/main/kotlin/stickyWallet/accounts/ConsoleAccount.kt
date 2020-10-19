package stickyWallet.accounts

import stickyWallet.configs.PluginConfiguration.CheckSettings
import stickyWallet.currencies.Currency
import java.math.BigDecimal
import java.util.UUID

class ConsoleAccount : Account(
    UUID.fromString("ddd22521-4c13-4239-93a5-a976bcfc614f"),
    CheckSettings.consoleName
) {
    override fun hasEnough(currency: Currency, amount: BigDecimal) = true
    override fun hasEnough(amount: BigDecimal) = true

    override fun withdraw(currency: Currency, amount: BigDecimal) = true
}
