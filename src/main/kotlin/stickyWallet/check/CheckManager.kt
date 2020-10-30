package stickyWallet.check

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import stickyWallet.StickyWallet
import stickyWallet.configs.PluginConfiguration.CheckSettings
import stickyWallet.currencies.Currency
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.StringUtilities
import java.math.BigDecimal

object CheckManager : UsePlugin {
    private val checkBaseItem: ItemStack

    val kCheckValue = NamespacedKey(pluginInstance, "stickywallet.check.value")
    val kCheckIssuer = NamespacedKey(pluginInstance, "stickywallet.check.issuer")
    val kCheckCurrency = NamespacedKey(pluginInstance, "stickywallet.check.currency")

    init {
        val item = ItemStack(CheckSettings.material, 1)
        val meta = item.itemMeta
        meta.setDisplayName(StringUtilities.colorize(CheckSettings.name))

        item.itemMeta = meta
        item.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1)
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
                        .replace("{currencycolor}", currency.color.toString())
                )
            }
        )

        val checkItem = this.checkBaseItem.clone()
        val meta = checkItem.itemMeta

        meta.lore = formatLore

        val store = meta.persistentDataContainer
        store.set(kCheckIssuer, PersistentDataType.STRING, finalCreatorName)
        store.set(kCheckCurrency, PersistentDataType.STRING, currency.plural)
        store.set(kCheckValue, PersistentDataType.STRING, amount.toString())

        checkItem.itemMeta = meta

        return checkItem
    }

    fun getCurrencyForCheck(currency: String) = pluginInstance.currencyStore.getCurrency(currency)

    fun validateCheck(item: ItemStack): CheckData? {
        if (item.type != CheckSettings.material) return null

        val meta = item.itemMeta
        val dataStore = meta.persistentDataContainer

        val checkValue = dataStore.get(kCheckValue, PersistentDataType.STRING)?.toBigDecimal()
        val checkIssuer = dataStore.get(kCheckIssuer, PersistentDataType.STRING)
        val checkCurrency = dataStore.get(kCheckCurrency, PersistentDataType.STRING)

        if (checkValue == null || checkValue <= BigDecimal.ZERO) return null
        if (checkIssuer == null) return null
        if (checkCurrency == null || !pluginInstance.currencyStore.currencyExists(checkCurrency)) return null

        return CheckData(
            checkValue,
            checkCurrency,
            checkIssuer
        )
    }

    fun logDebugCheck(item: ItemStack, player: Player) {
        val meta = item.itemMeta
        val dataStore = meta.persistentDataContainer

        val checkValue = dataStore.get(kCheckValue, PersistentDataType.STRING)?.toBigDecimal()
        val checkIssuer = dataStore.get(kCheckIssuer, PersistentDataType.STRING)
        val checkCurrency = dataStore.get(kCheckCurrency, PersistentDataType.STRING)
        val currencyExists = checkCurrency?.let { pluginInstance.currencyStore.currencyExists(it) }
        val itemMaterialMatch = item.type === CheckSettings.material

        player.sendMessage(
            """
           ---------------------------------------------
                      StickyWallet Check Debug

            Item String         : $item

            Material Matches    : $itemMaterialMatch
            Check Value         : $checkValue
            Check Issuer        : $checkIssuer
            Check Currency      : $checkCurrency
            Currency Exists     : $currencyExists
            Available Currencies: ${StickyWallet.instance.currencyStore.currencies.joinToString { it.plural }}
           ---------------------------------------------
            """.trimIndent()
        )
    }

    data class CheckData(
        val value: BigDecimal,
        val currency: String,
        val issuer: String
    )
}
