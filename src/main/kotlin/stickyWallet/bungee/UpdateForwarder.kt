package stickyWallet.bungee

import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import stickyWallet.StickyWallet
import stickyWallet.configs.PluginConfiguration
import stickyWallet.interfaces.UsePlugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.UUID

object UpdateForwarder : PluginMessageListener, UsePlugin {
    private val channelName = "StickyWallet Data Channel"

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "BungeeCord") return

        val dataIn = ByteStreams.newDataInput(message)
        val subChannel = dataIn.readUTF()

        if (subChannel == channelName) {
            val (type, name) = dataIn.readUTF().split(",")

            logIfEnabled("$channelName received $type => $name")

            when (type.toLowerCase()) {
                "currency" -> {
                    val uuid = UUID.fromString(name)
                    val currency = pluginInstance.currencyStore.getCurrency(uuid)

                    if (currency != null) {
                        StickyWallet.doAsync {
                            pluginInstance.dataHandler.updateCachedCurrency(currency)
                        }
                        logIfEnabled("$channelName: Updated currency ${currency.plural} ($name)")
                    }
                }
                "account" -> {
                    val uuid = UUID.fromString(name)
                    StickyWallet.doAsync {
                        pluginInstance.accountStore.removeCachedAccount(uuid)
                        pluginInstance.dataHandler.loadAccount(uuid)
                    }
                    logIfEnabled("$channelName: Account $name reloaded")
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
            messageOut.writeUTF("${type.toLowerCase()},$name")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        out.write(messageBytes.toByteArray())

        Bukkit.getOnlinePlayers().firstOrNull()
            ?.sendPluginMessage(pluginInstance, "BungeeCord", out.toByteArray())
    }

    private fun logIfEnabled(message: String) {
        if (PluginConfiguration.DebugSettings.logsEnabled) pluginInstance.logger.info(message)
    }
}
