package stickyWallet.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
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

        val caughtItems = mutableListOf<ItemStack>()

        for (item in matrix) {
            // If there's nothing in the slot, continue
            if (item == null) continue
            if (checkItemStackIsCheck(item)) caughtItems.add(item)
        }

        if (caughtItems.isNotEmpty()) {
            caughtItems.forEach {
                if (Random.nextDouble(0.0, 1.0) <= 0.15) {
                    // Bye bye check
                    event.whoClicked.inventory.remove(it)
                }
            }
            event.whoClicked.sendMessage(
                colorize("${L.prefix}${PluginConfiguration.CheckSettings.noCraftingWithChecks}")
            )
            event.isCancelled = true
            event.whoClicked.closeInventory()
        }
    }

    private fun checkItemStackIsCheck(input: ItemStack): Boolean {
        CheckManager.validateCheck(input) ?: return false
        return true
    }
}