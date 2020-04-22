package stickyWallet.check

import StickyPlugin
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import stickyWallet.currency.Currency
import stickyWallet.nbt.NBTItem
import stickyWallet.utils.StringUtils

class CheckManager(private val plugin: StickyPlugin) {

    private val checkBaseItem: ItemStack

    init {
        val item = ItemStack(Material.valueOf(plugin.config.getString("check.material")!!), 1)
        val meta = item.itemMeta
        meta.setDisplayName(StringUtils.colorize(plugin.config.getString("check.name")!!))
        meta.lore = StringUtils.colorize(plugin.config.getStringList("check.lore"))
        item.itemMeta = meta
        item.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1)
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        this.checkBaseItem = item
    }

    fun write(creatorName: String, currency: Currency, amount: Double): ItemStack? {
        if (!currency.payable) return null

        val finalCreatorName = if (creatorName == "CONSOLE") {
            StringUtils.colorize(plugin.config.getString("check.console_name")!!)
        } else creatorName

        val formatLore = mutableListOf<String>()

        for (baseLore in this.checkBaseItem.itemMeta.lore!!) {
            formatLore.add(
                baseLore.replace("{value}", currency.format(amount))
                    .replace("{player}", finalCreatorName)
            )
        }

        val ret = this.checkBaseItem.clone()
        val nbt = NBTItem(ret)
        val meta = nbt.bukkitItem.itemMeta
        meta.lore = formatLore
        nbt.bukkitItem.itemMeta = meta
        nbt.setString(nbtIssuer, creatorName)
        nbt.setString(nbtCurrency, currency.plural)
        nbt.setString(nbtValue, amount.toString())

        return nbt.bukkitItem
    }

    fun isValid(itemStack: NBTItem): Boolean {
        if (itemStack.bukkitItem.type != this.checkBaseItem.type) return false
        return try {
            when {
                itemStack.getString(nbtValue) == null -> false
                itemStack.getString(nbtIssuer) == null -> false
                itemStack.getString(nbtCurrency) == null -> false
                else -> {
                    val original = this.checkBaseItem.itemMeta.displayName
                    val currentMeta = itemStack.bukkitItem.itemMeta ?: return false

                    if (currentMeta.hasDisplayName() && currentMeta.displayName != original) return false
                    if (currentMeta.hasLore() && currentMeta.lore?.size ?: 0 == checkBaseItem.lore?.size ?: -1) return true

                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getChequeValue(itemStack: NBTItem): Double {
        return try {
            when {
                itemStack.getString(nbtValue) == null -> -1.0
                itemStack.getString(nbtIssuer) == null -> -1.0
                itemStack.getString(nbtCurrency) == null -> -1.0
                else -> itemStack.getString(nbtCurrency)!!.toDouble()
            }
        } catch (_: Exception) {
            0.0
        }
    }

    fun getCurrencyForCheck(itemStack: NBTItem): Currency? {
        return try {
            when {
                itemStack.getString(nbtValue) == null -> this.plugin.currencyManager.getDefaultCurrency()
                itemStack.getString(nbtIssuer) == null -> this.plugin.currencyManager.getDefaultCurrency()
                itemStack.getString(nbtCurrency) == null -> this.plugin.currencyManager.getDefaultCurrency()
                else -> this.plugin.currencyManager.getCurrency(itemStack.getString(nbtCurrency)!!)
            }
        } catch (_: Exception) {
            this.plugin.currencyManager.getDefaultCurrency()
        }
    }

    companion object {
        const val nbtIssuer = "issuer"
        const val nbtValue = "value"
        const val nbtCurrency = "currency"
    }

}