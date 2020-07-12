package stickyWallet.vault

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import stickyWallet.interfaces.UsePlugin

object VaultManager : UsePlugin {
    private var hook: VaultHook? = null

    fun hook() {
        try {
            if (this.hook == null) this.hook = VaultHook()

            if (pluginInstance.currencyStore.getDefaultCurrency() == null) {
                pluginInstance.logger.info("No default currency found; Vault linking has been disabled")
                return
            }

            val sm = Bukkit.getServicesManager()
            sm.register(Economy::class.java, this.hook!!, pluginInstance, ServicePriority.Highest)

            pluginInstance.logger.info("Vault support has been enabled")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unhook() {
        val sm = Bukkit.getServicesManager()
        if (this.hook != null) {
            sm.unregister(Economy::class.java, this.hook!!)
            this.hook = null
        }
    }
}
