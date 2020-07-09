package newStickyWallet

import org.bukkit.plugin.java.JavaPlugin

class StickyWalletV2 : JavaPlugin() {

    override fun onLoad() {
        logger.info("Loading plugin $name, version ${description.version}")
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    companion object {
        lateinit var instance: StickyWalletV2

        fun doAsync(runnable: Runnable) {
            instance.server.scheduler.runTaskAsynchronously(instance, runnable)
        }

        fun doSync(runnable: Runnable) {
            instance.server.scheduler.runTask(instance, runnable)
        }

        fun doLater(runnable: Runnable, after: Long) {
            instance.server.scheduler.runTaskLater(instance, runnable, after)
        }
    }
}
