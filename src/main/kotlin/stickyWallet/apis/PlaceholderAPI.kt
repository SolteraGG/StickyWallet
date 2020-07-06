package stickyWallet.apis

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import stickyWallet.StickyPlugin

class PlaceholderAPI : PlaceholderExpansion() {
    private val plugin = StickyPlugin.instance

    override fun getVersion() = plugin.description.version

    override fun getAuthor() = plugin.description.authors.joinToString(", ")

    override fun getIdentifier() = "stickywallet"

    override fun persist() = true

    override fun canRegister() = true

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) return ""

        val split = identifier.toLowerCase().split(".")

        if (split.size >= 2) {
            if (!split[0].equals("balances", true)) return null

            val currencyName = split[1]
            val formatted = try {
                split[2]
                true
            } catch (e: Exception) {
                false
            }

            val currency = plugin.currencyManager.getCurrency(currencyName) ?: return null
            val account = plugin.accountManager.getAccount(player) ?: return "0"

            val balance = account.getBalance(currency)


        }

        return null
    }

}