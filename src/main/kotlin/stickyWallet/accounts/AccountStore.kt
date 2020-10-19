package stickyWallet.accounts

import org.bukkit.entity.Player
import stickyWallet.interfaces.UsePlugin
import java.util.UUID

object AccountStore : UsePlugin {
    val accounts = mutableListOf<Account>()

    fun getAccount(player: Player) = getAccount(player.uniqueId)

    fun getAccount(nickname: String) = accounts.find {
        it.playerName?.equals(nickname, true) ?: false
    } ?: pluginInstance.dataHandler.loadAccount(nickname)

    fun getAccount(uuid: UUID) = accounts.find {
        it.uuid == uuid
    } ?: pluginInstance.dataHandler.loadAccount(uuid)

    fun removeCachedAccount(uuid: UUID) = accounts.removeIf {
        it.uuid == uuid
    }

    fun addCachedAccount(account: Account) = if (accounts.contains(account)) {
        false
    } else {
        accounts.add(account)
    }
}
