package stickyWallet.sql.tables

import org.bukkit.ChatColor
import org.jetbrains.exposed.sql.Table
import stickyWallet.configs.PluginConfiguration
import stickyWallet.sql.PostgresDecimal
import java.math.BigDecimal

object CurrenciesTable : Table(
    PluginConfiguration.StorageSettings.storageTablePrefix + "_currencies"
) {
    val id = integer("id").autoIncrement()
    val uuid = varchar("uuid", 255)
    val type = varchar("type", 255)
    val singular = varchar("name_singular", 512)
    val plural = varchar("name_plural", 512)
    val symbol = varchar("symbol", 10).nullable()
    val color = varchar("color", 255).default(ChatColor.WHITE.toString())
    val decimalSupported = bool("decimals_supported").default(false)
    val defaultCurrency = bool("default_currency").default(false)
    val defaultBalance = registerColumn<BigDecimal>("default_balance", PostgresDecimal()).default(BigDecimal(0.0))
    val exchangeRate = registerColumn<BigDecimal>("exchange_rate", PostgresDecimal()).default(BigDecimal(1.0))

    override val primaryKey = PrimaryKey(singular, plural)
}