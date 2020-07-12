package stickyWallet.sql.tables

import org.jetbrains.exposed.sql.Table
import stickyWallet.configs.PluginConfiguration

object BalancesTable : Table (
  PluginConfiguration.StorageSettings.storageTablePrefix + "_balances"
) {
    val accountID = varchar("account_id", 255)
    val currencyID = varchar("currency_id", 255)
    val balance = double("balance")

    override val primaryKey = PrimaryKey(accountID, currencyID)
}