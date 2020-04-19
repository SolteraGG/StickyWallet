package cheque

import StickyWallet
import currency.Currency
import nbt.NBTItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import utils.StringUtils
import java.lang.Exception
import java.util.*

class ChequeManager(private val plugin: StickyWallet) {

    private val chequeBaseItem: ItemStack

    init {
        val item = ItemStack(Material.valueOf(plugin.config.getString("cheque.material")!!), 1)
        val meta = item.itemMeta
        meta.setDisplayName(StringUtils.colorize(plugin.config.getString("cheque.name")!!))
        meta.lore = StringUtils.colorize(plugin.config.getStringList("cheque.lore"))
        item.itemMeta = meta

        this.chequeBaseItem = item
    }

    fun write(creatorName: String, currency: Currency, amount: Double): ItemStack? {
        if (!currency.payable) return null

        val finalCreatorName = if (creatorName == "CONSOLE") {
            StringUtils.colorize(plugin.config.getString("cheque.console_name")!!)
        } else creatorName

        val formatLore = mutableListOf<String>()

        for (baseLore in Objects.requireNonNull(this.chequeBaseItem.itemMeta.lore)) {
            formatLore.add(
                baseLore.replace("{value}", currency.format(amount))
                    .replace("{player}", finalCreatorName)
            )
        }

        val ret = this.chequeBaseItem.clone()
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
        if (itemStack.bukkitItem.type != this.chequeBaseItem.type) return false
        return try {
            when {
                itemStack.getString(nbtValue) == null -> false
                itemStack.getString(nbtIssuer) == null -> false
                itemStack.getString(nbtCurrency) == null -> false
                else -> {
                    val original = this.chequeBaseItem.itemMeta.displayName
                    val currentMeta = itemStack.bukkitItem.itemMeta ?: return false

                    if (currentMeta.hasDisplayName() && currentMeta.displayName != original) return false
                    if (currentMeta.hasLore() && currentMeta.lore?.size ?: 0 == chequeBaseItem.lore?.size ?: -1) return true

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

    fun getCurrencyForCheque(itemStack: NBTItem): Currency? {
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
        private const val nbtIssuer = "issuer"
        private const val nbtValue = "value"
        private const val nbtCurrency = "currency"
    }

}