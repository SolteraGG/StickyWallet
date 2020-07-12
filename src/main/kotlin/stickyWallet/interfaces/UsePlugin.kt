package stickyWallet.interfaces

import stickyWallet.StickyWallet
import stickyWallet.configs.PluginConfiguration
import stickyWallet.utils.StickyConsole

interface UsePlugin {
    val pluginInstance: StickyWallet
        get() = StickyWallet.instance

    val pluginLogger: StickyConsole
        get() = StickyWallet.instance.logger

    val pluginTranslations: PluginConfiguration.Translations
        get() = PluginConfiguration.Translations
}
