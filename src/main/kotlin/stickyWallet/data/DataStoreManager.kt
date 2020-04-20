package stickyWallet.data

import StickyPlugin
import java.io.File

class DataStoreManager {

    val plugin = StickyPlugin.instance
    val methods = mutableListOf<DataStore>()

    fun getStore(name: String) = methods.find { it.name.equals(name, true) }

    private fun addStore(store: DataStore) = if (this.methods.contains(store)) {
        false
    } else {
        this.methods.add(store)
        true
    }

    init {
        plugin.economyLogger.log("Loading the yaml and MySQL stores")
        addStore(YamlStorage(File(plugin.dataFolder, "data.yml")))
        // TODO: OurSQL
    }

}