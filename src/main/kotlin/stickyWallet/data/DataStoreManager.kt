package stickyWallet.data

import java.io.File
import stickyWallet.StickyPlugin

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
        addStore(MySQLStorage(
            plugin.config.getString("mysql.host", "localhost")!!,
            plugin.config.getInt("mysql.port", 3306),
            plugin.config.getString("mysql.database", "minecraft")!!,
            plugin.config.getString("mysql.username", "root")!!,
            plugin.config.getString("mysql.password", "password")!!
        ))
    }
}
