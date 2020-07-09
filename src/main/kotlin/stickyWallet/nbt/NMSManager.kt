package stickyWallet.nbt

import org.bukkit.Bukkit
import org.bukkit.ChatColor

class NMSManager {

    private val versionMap: MutableMap<Int, String> = HashMap()
    private var versionID: Int

    init {
        this.loadVersions()

        val packageName = Bukkit.getServer().javaClass.`package`.name
        val version = packageName.substring(packageName.lastIndexOf('.') + 1)

        if (versionMap.containsValue(version)) {
            this.versionID = this.getVersionID(version)
        } else {
            this.versionID = 0
            Bukkit.getConsoleSender().sendMessage("${ChatColor.DARK_RED}----------------------------------------------------------")
            Bukkit.getConsoleSender().sendMessage("")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.DARK_RED}${ChatColor.BOLD}YOU ARE RUNNING AN UNSUPPORTED VERSION OF SPIGOT!")
            Bukkit.getConsoleSender().sendMessage("")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}StickyWallet Checks functionality will at best be limited. Please do come")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}complaining to me, the developer of StickyWallet, when something breaks,")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}because running an unsupported version will cause exactly this. I do")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}in no way accept responsibility for ANY damage caused to a server running")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}an unsupported version of Spigot. It is recommended that you change to")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}a supported version of Spigot. Supported versions are 1.13, 1.14 & 1.15.")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}Versions marked with an asterisk (*) may have limited functionality.")
            Bukkit.getConsoleSender().sendMessage("")
            Bukkit.getConsoleSender().sendMessage("${ChatColor.DARK_RED}----------------------------------------------------------")
        }
    }

    private fun loadVersions() {
        registerVersion(UNSUPPORTED)
        registerVersion(V1_9_R1)
        registerVersion(V1_9_R2)
        registerVersion(V1_10_R1)
        registerVersion(V1_11_R1)
        registerVersion(V1_12_R1)
        registerVersion(V1_13_R1)
        registerVersion(V1_13_R2)
        registerVersion(V1_14_R1)
        registerVersion(V1_15_R1)
        registerVersion(V1_16_R1)
    }

    private fun registerVersion(string: String) {
        this.versionMap[this.versionMap.size] = string
    }

    private fun getVersionID(version: String): Int {
        return this.versionMap.entries.parallelStream()
            .filter { it.value.equals(version, true) }
            .findFirst().map { it.key }.orElse(0)
    }

    fun getVersionString() = this.getVersionString(this.versionID)

    private fun getVersionString(id: Int) = this.versionMap[id]!!

    companion object {
        private const val UNSUPPORTED = "Unsupported"
        private const val V1_9_R1 = "v1_9_R1"
        private const val V1_9_R2 = "v1_9_R2"
        private const val V1_10_R1 = "v1_10_R1"
        private const val V1_11_R1 = "v1_11_R1"
        private const val V1_12_R1 = "v1_12_R1"
        private const val V1_13_R1 = "v1_13_R1"
        private const val V1_13_R2 = "v1_13_R2"
        private const val V1_14_R1 = "v1_14_R1"
        private const val V1_15_R1 = "v1_15_R1"
        private const val V1_16_R1 = "v1_16_R1"
    }
}
