package stickyWallet.check

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import stickyWallet.configs.PluginConfiguration.CheckSettings
import stickyWallet.currencies.Currency
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.StringUtilities
import java.math.BigDecimal

object CheckManager : UsePlugin {
    private val checkBaseItem: ItemStack

    val checkValueKey = NamespacedKey(pluginInstance, "stickywallet.check.value")
    val checkIssuerKey = NamespacedKey(pluginInstance, "stickywallet.check.issuer")
    val checkCurrencyKey = NamespacedKey(pluginInstance, "stickywallet.check.currency")

    init {
        val item = ItemStack(CheckSettings.material, 1)
        val meta = item.itemMeta
        meta.setDisplayName(StringUtilities.colorize(CheckSettings.name))

        item.itemMeta = meta
        item.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 0)
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        this.checkBaseItem = item
    }

    fun write(creatorName: String, currency: Currency, amount: BigDecimal): ItemStack {
        val finalCreatorName = if (creatorName.equals("console", true)) {
            CheckSettings.consoleName
        } else creatorName

        val formatLore = mutableListOf<String>()

        val loreToParse = if (creatorName.equals("console", true)) {
            CheckSettings.consoleLore
        } else {
            CheckSettings.lore
        }

        formatLore.addAll(
            loreToParse.map {
                StringUtilities.colorize(
                    it
                        .replace("{value}", currency.format(amount))
                        .replace("{player}", finalCreatorName)
                )
            }
        )

        val checkItem = this.checkBaseItem.clone()
        val meta = checkItem.itemMeta

        meta.lore = formatLore

        val store = meta.persistentDataContainer
        store.set(checkIssuerKey, PersistentDataType.STRING, finalCreatorName)
        store.set(checkCurrencyKey, PersistentDataType.STRING, currency.plural)
        store.set(checkValueKey, PersistentDataType.STRING, amount.toString())

        checkItem.itemMeta = meta

        return checkItem
    }

    fun getCurrencyForCheck(currency: String) = pluginInstance.currencyStore.getCurrency(currency)

    fun validateCheck(item: ItemStack): CheckData? {
        val meta = item.itemMeta

        val dataStore = meta.persistentDataContainer

        val checkValue = dataStore.get(checkValueKey, PersistentDataType.STRING)?.toBigDecimal()
        val checkIssuer = dataStore.get(checkIssuerKey, PersistentDataType.STRING)
        val checkCurrency = dataStore.get(checkCurrencyKey, PersistentDataType.STRING)

        if (checkValue == null || checkValue <= BigDecimal.ZERO) return null
        if (checkIssuer == null) return null
        if (checkCurrency == null || !pluginInstance.currencyStore.currencyExists(checkCurrency)) return null

        return CheckData(
            checkValue,
            checkCurrency,
            checkIssuer
        )
    }

    data class CheckData(
        val value: BigDecimal,
        val currency: String,
        val issuer: String
    )
}