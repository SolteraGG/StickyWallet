package stickyWallet.utils

import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import stickyWallet.sql.tables.AccountsTable

object Utilities {
    fun getPlayerNames(): MutableList<String> {
        val playerNames = mutableSetOf<String>()

        playerNames.addAll(Bukkit.getOnlinePlayers().map { it.name }.sorted())

        val accountNames = transaction {
            AccountsTable
                .slice(AccountsTable.playerName)
                .selectAll()
                .orderBy(AccountsTable.playerName to SortOrder.ASC)
                .map { it[AccountsTable.playerName] }
                .toSet()
        }

        playerNames.addAll(accountNames)

        return playerNames.toMutableList()
    }
}
