package stickyWallet.configs

import org.bukkit.Material
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Constants.SettingsPaths

object PluginConfiguration : UsePlugin {
    fun initialize() {
        val config = pluginInstance.config

        // Set up the config header
        config.options().header("""
            ${pluginInstance.description.name} v${pluginInstance.description.version}
            
            The main configuration file for ${pluginInstance.description.name}
            
            Developer(s): ${pluginInstance.description.authors.joinToString(", ")}
            
            ---
            
            storage:
              - database     : The database name to use in postgres - case sensitive; default: "stickywallet"
              - tablePrefix  : The table prefix used when creating tables - case sensitive; default: "stickywallet"
              - host         : The host for the database; default: "localhost"
              - port         : The port for the database; default: 3306
              - username     : The username for the database; default: "postgres"
              - password     : The password for the database; default: "password"
              - currencyTypes: The types of currencies to load; default: ["global"]
                          
            check:
              - material: The material to use for the check item - list available here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html; default: "PAPER"
              - name    : The name of the check in the user's inventory; default: "&aBank Note"
              - lore    : The lore of the check in the user's inventory; default: ["&7Worth: {currencycolor}{value}.", "&7&oWritten by &6{player}"]
                - "{value}" will be replaced with the check value and currency
                - "{player}" will be replaced with the player that wrote this check
                - "{currencycolor}" will be replaced with the currency color
              - consoleName: The name checks get when written by the console; default: "&kOwO &r&l&4The Master Itself &kOwO"
              - consoleLore: Same as "lore", except used when the console writes a check; default: ["&l&dGiven by &r&kOwO &r&l&4The Master Itself &kOwO&r&l&d; you should cherish it", "&7Worth: {value}."]
              
            integrations:
              - vault: If VaultAPI support should be enabled; default: true
              
        """.trimIndent())

    //    START: Storage
        addDefault(SettingsPaths.StorageDatabase, "stickywallet")
        addDefault(SettingsPaths.StorageTablePrefix, "stickywallet")
        addDefault(SettingsPaths.StorageHost, "localhost")
        addDefault(SettingsPaths.StoragePort, 3306)
        addDefault(SettingsPaths.StorageUsername, "postgres")
        addDefault(SettingsPaths.StoragePassword, "password")
        addDefault(SettingsPaths.StorageCurrencyTypes, listOf("global"))
    //    END: Storage

    //    START: Check
        addDefault(SettingsPaths.CheckMaterial, Material.PAPER.toString())
        addDefault(SettingsPaths.CheckName, "&aBank Note")
        addDefault(SettingsPaths.CheckLore, listOf("&7Worth: {currencycolor}{value}.", "&7&oWritten by &6{player}"))
        addDefault(SettingsPaths.CheckConsoleName, "&kOwO &r&l&4The Master Itself &kOwO")
        addDefault(
            SettingsPaths.CheckConsoleLore,
            listOf("&l&dGiven by &r&l&4&kOwO &r&l&4The Master Itself &kOwO&r&l&d; you should cherish it", "&7Worth: {value}.")
        )
        addDefault(
            SettingsPaths.CheckNoCrafting,
            "&o&6Seems like some of your checks might have magically &4dissappeared&6 after your attempt at crafting items with them..."
        )
    //    END: Check

    //    START: Debug
        addDefault(SettingsPaths.DebugLogs, false)
        addDefault(SettingsPaths.DebugTransactions, false)
    //    END: Debug

    //    START: Integrations
        addDefault(SettingsPaths.VaultIntegration, true)
    //    END: Integrations

    //    START: Translations...

    //    END: Translations...phew

        config.options().copyDefaults(true)
        pluginInstance.saveConfig()
        pluginInstance.reloadConfig()
    }

    private fun addDefault(key: String, value: Any) = pluginInstance.config.addDefault(key, value)

//    START: Field Definition

    object StorageSettings {
        val storageDatabase
            get() = pluginInstance.config.getString(SettingsPaths.StorageDatabase, "stickywallet")!!

        val storageTablePrefix
            get() = pluginInstance.config.getString(SettingsPaths.StorageTablePrefix, "stickywallet")!!

        val storageHost
            get() = pluginInstance.config.getString(SettingsPaths.StorageHost, "localhost")!!

        val storagePort: Number
            get() = pluginInstance.config.getInt(SettingsPaths.StoragePort, 3306)

        val storageUsername
            get() = pluginInstance.config.getString(SettingsPaths.StorageUsername, "root")!!

        val storagePassword
            get() = pluginInstance.config.getString(SettingsPaths.StoragePassword, "password")!!

        val currencyTypes: List<String>
            get() = pluginInstance.config.getStringList(SettingsPaths.StorageCurrencyTypes)
    }

    object CheckSettings {
        val material
            get() = Material.getMaterial(
                pluginInstance.config.getString(SettingsPaths.CheckMaterial, "PAPER")!!
            ).let { it ?: Material.PAPER }

        val name
            get() = pluginInstance.config.getString(SettingsPaths.CheckName, "&aBank Note")!!

        val lore: List<String>
            get() = pluginInstance.config.getStringList(SettingsPaths.CheckLore)

        val consoleName
            get() = pluginInstance.config.getString(SettingsPaths.CheckConsoleName, "&k&l&4OwO &r&l&4The Master Itself &kOwO")!!

        val consoleLore: List<String>
            get() = pluginInstance.config.getStringList(SettingsPaths.CheckConsoleLore)

        val noCraftingWithChecks
            get() = pluginInstance.config.getString(SettingsPaths.CheckNoCrafting, "&o&6Seems like some of your checks might have magically &4dissappeared&6 after your attempt at crafting items with them...")
    }

    object DebugSettings {
        val logsEnabled
            get() = pluginInstance.config.getBoolean(SettingsPaths.DebugLogs, false)

        val transactionLogs
            get() = pluginInstance.config.getBoolean(SettingsPaths.DebugTransactions, false)
    }

    object IntegrationSettings {
        val vaultEnabled
            get() = pluginInstance.config.getBoolean(SettingsPaths.VaultIntegration, true)
    }

    object Translations

//    END: Field Definition
}
