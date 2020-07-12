package stickyWallet.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import stickyWallet.StickyWallet
import stickyWallet.accounts.Account
import stickyWallet.configs.L
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions

class JoinLeaveListeners : UsePlugin, Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player

        if (event.result != PlayerLoginEvent.Result.ALLOWED) return

        StickyWallet.doAsync {
            var account = pluginInstance.accountStore.getAccount(player.uniqueId)

            if (account == null) {
                pluginInstance.logger.info("Creating account for ${player.name} (${player.uniqueId})")

                account = Account(player.uniqueId, player.name)
                pluginInstance.dataHandler.saveAccount(account)
                pluginInstance.accountStore.addCachedAccount(account)
            } else if (account.playerName == null || !account.playerName.equals(player.name)) {
                val old = account.playerName
                account.playerName = player.name

                pluginInstance.dataHandler.saveAccount(account)

                pluginInstance.logger.info("""
                    Name change found for player ${player.name} with UUID ${player.uniqueId}
                    Old: $old
                    New: ${player.name}
                """.trimIndent())
            }

            pluginInstance.accountStore.addCachedAccount(account)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        pluginInstance.accountStore.removeCachedAccount(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        StickyWallet.doLater(60) {
            if (pluginInstance.currencyStore.getDefaultCurrency() == null && (player.isOp || player.hasPermission(Permissions.COMMAND_CURRENCY))) {
                player.sendMessage("${L.prefix}You don't have a currency created yet! Please create one by running \"§e/currency§r\".")
            }
        }
    }
}