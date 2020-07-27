package stickyWallet.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import stickyWallet.configs.PluginConfiguration.CheckSettings
import stickyWallet.interfaces.UsePlugin

class ItemUse : UsePlugin, Listener {
    @EventHandler()
    fun onItemUse(event: PlayerInteractEvent) {
        val player = event.player

        val item = event.item ?: return

        if (item.type == CheckSettings.material) {
            player.sendActionBar("You have a check-like thing owo")
        }
    }
}
