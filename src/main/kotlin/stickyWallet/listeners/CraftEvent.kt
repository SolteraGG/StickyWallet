package stickyWallet.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import stickyWallet.StickyWallet
import stickyWallet.check.CheckManager
import stickyWallet.configs.L
import stickyWallet.configs.PluginConfiguration
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.StringUtilities.colorize
import kotlin.random.Random

class CraftEvent : UsePlugin, Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onCraft(event: CraftItemEvent) {
        val matrix = event.inventory.matrix

        var shouldAlert = false

        for (item in matrix) {
            // If there's nothing in the slot, continue
            if (item == null) continue
            if (checkItemStackIsCheck(item)) {
                event.isCancelled = true
                shouldAlert = true
                if (Random.nextDouble(0.0, 100.0) <= 15.0) {
                    // Bye bye check
                    item.subtract()
                }
            }
        }

        if (shouldAlert) {
            StickyWallet.doLater(2) {
                event.whoClicked.closeInventory()
                event.whoClicked.sendMessage(
                    colorize("${L.prefix}${PluginConfiguration.CheckSettings.noCraftingWithChecks}")
                )
            }
        }
    }

    private fun checkItemStackIsCheck(input: ItemStack): Boolean {
        CheckManager.validateCheck(input) ?: return false
        return true
    }
}
