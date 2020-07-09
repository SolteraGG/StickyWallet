package stickyWallet.accounts

import java.util.UUID
import org.bukkit.entity.Player
import stickyWallet.StickyPlugin

class AccountManager(private val plugin: StickyPlugin) {

    val accounts = mutableListOf<Account>()

    fun getAccount(player: Player) = getAccount(player.uniqueId)

    fun getAccount(nickname: String): Account? {
        return accounts.find {
            it.nickname?.equals(nickname, true) ?: false
        } ?: plugin.dataStore.loadAccount(nickname)
    }

    fun getAccount(uuid: UUID): Account? {
        return accounts.find {
            it.uuid == uuid
        } ?: plugin.dataStore.loadAccount(uuid)
    }

    fun removeAccount(uuid: UUID) {
        accounts.removeIf {
            it.uuid == uuid
        }
    }

    fun addAccount(account: Account) = if (accounts.contains(account)) {
        false
    } else {
        accounts.add(account)
    }

    fun getAllAccounts() = plugin.dataStore.loadAllAccounts()
}
