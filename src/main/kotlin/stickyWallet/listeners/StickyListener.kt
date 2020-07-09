package stickyWallet.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import stickyWallet.StickyPlugin
import stickyWallet.accounts.Account
import stickyWallet.files.L
import stickyWallet.utils.Permissions
import stickyWallet.utils.ServerUtils

class StickyListener : Listener {

    private val plugin = StickyPlugin.instance

    @EventHandler(priority = EventPriority.LOWEST)
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player

        if (event.result != PlayerLoginEvent.Result.ALLOWED) return

        StickyPlugin.doAsync(Runnable {
            var account = plugin.accountManager.getAccount(player.uniqueId)

            if (account == null) {
                ServerUtils.log("Creating account for ${player.name} (${player.uniqueId})")
                account = Account(player.uniqueId, player.name)

                if (!plugin.dataStore.name.equals("yaml", true)) {
                    plugin.dataStore.createAccount(account)
                } else {
                    plugin.dataStore.saveAccount(account)
                }

                plugin.accountManager.addAccount(account)
            } else if (account.nickname == null || !account.nickname.equals(player.name)) {
                val old = account.nickname
                account.nickname = player.name
                plugin.dataStore.saveAccount(account)
                ServerUtils.log("""
                    Name change found for player ${player.name} with UUID ${player.uniqueId}
                    Old: $old
                    New: ${account.displayName}
                """.trimIndent())
            }
        })
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.accountManager.removeAccount(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        StickyPlugin.doSync(Runnable {
            val account = plugin.accountManager.accounts.find {
                it.uuid == player.uniqueId
            }
            if (account != null) plugin.accountManager.addAccount(account)
        })

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (plugin.currencyManager.getDefaultCurrency() == null && (player.isOp || player.hasPermission(Permissions.COMMAND_CURRENCY))) {
                player.sendMessage("${L.prefix}§cYou don't have a currency created yet! Please create a currency by running \"§e/currency§c\".")
            }
        }, 60)
    }
}
