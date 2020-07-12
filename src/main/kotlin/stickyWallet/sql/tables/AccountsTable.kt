package stickyWallet.sql.tables

import org.jetbrains.exposed.sql.Table
import stickyWallet.configs.PluginConfiguration

object AccountsTable : Table(
    PluginConfiguration.StorageSettings.storageTablePrefix + "_accounts"
) {
    val playerName = varchar("player_name", 255)
    val playerUUID = varchar("player_uuid", 255)

    override val primaryKey = PrimaryKey(playerUUID)
}
