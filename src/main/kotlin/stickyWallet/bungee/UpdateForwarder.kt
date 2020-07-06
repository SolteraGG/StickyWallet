package stickyWallet.bungee

import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import stickyWallet.StickyPlugin
import stickyWallet.utils.ServerUtils
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class UpdateForwarder(private val plugin: StickyPlugin) : PluginMessageListener {

    private val channelName = "StickyWallet Data Channel"

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "BungeeCord") return

        val dataIn = ByteStreams.newDataInput(message)
        val subChannel = dataIn.readUTF()

        if (subChannel == channelName) {
            val (type, name) = dataIn.readUTF().split(",")

            if (plugin.debug) ServerUtils.log("$channelName received $type => $name")

            when (type) {
                "currency" -> {
                    val uuid = UUID.fromString(name)
                    val currency = plugin.currencyManager.getCurrency(uuid)

                    if (currency != null) {
                        try {
                            plugin.dataStore.updateCurrencyLocally(currency)
                        } catch (_: Exception) {
                            // Ignore error
                        }
                        if (plugin.debug) ServerUtils.log("$channelName: Updated currency ${currency.plural} ($name)")
                    }
                }
                "account" -> {
                    val uuid = UUID.fromString(name)
                    plugin.accountManager.removeAccount(uuid)
                    StickyPlugin.doAsync(Runnable { plugin.dataStore.loadAccount(uuid) })
                    if (plugin.debug) ServerUtils.log("$channelName: Account $name reloaded")
                }
            }
        }
    }

    fun sendUpdateMessage(type: String, name: String) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Forward")
        out.writeUTF("ONLINE")
        out.writeUTF(channelName)

        val messageBytes = ByteArrayOutputStream()
        val messageOut = DataOutputStream(messageBytes)
        try {
            messageOut.writeUTF("$type,$name")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        out.write(messageBytes.toByteArray())

        Bukkit.getOnlinePlayers().firstOrNull()
            ?.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }

}