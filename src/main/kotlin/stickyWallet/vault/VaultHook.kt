package stickyWallet.vault

import StickyPlugin
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import stickyWallet.utils.ServerUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class VaultHook : AbstractEconomy() {

    override fun getBanks(): MutableList<String> = ArrayList()

    override fun getName() = "DDD Pawllet"

    override fun isBankOwner(name: String?, playerName: String?) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun bankDeposit(name: String?, amount: Double) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun bankWithdraw(name: String?, amount: Double) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun deleteBank(name: String?) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun createBank(name: String?, player: String?) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun isBankMember(name: String?, playerName: String?) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun currencyNameSingular() = StickyPlugin.instance.currencyManager.getDefaultCurrency()?.singular ?: "Unknown"

    override fun bankHas(name: String?, amount: Double) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun currencyNamePlural() = StickyPlugin.instance.currencyManager.getDefaultCurrency()?.plural ?: "Unknown"

    override fun isEnabled() = true

    override fun fractionalDigits() = -1

    override fun bankBalance(name: String?) = EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
    )

    override fun format(amount: Double) = StickyPlugin.instance.currencyManager.getDefaultCurrency()?.format(amount) ?: amount.toString()

    override fun hasBankSupport() = false

    override fun createPlayerAccount(playerName: String?): Boolean = false

    override fun createPlayerAccount(player: OfflinePlayer?): Boolean = false

    override fun createPlayerAccount(player: OfflinePlayer?, worldName: String?): Boolean = false

    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean = false

    override fun has(playerName: String, amount: Double): Boolean {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up if player $playerName has $amount money")
        val user = StickyPlugin.instance.accountManager.getAccount(playerName)
        if (user != null) return user.hasEnough(amount)
        return false
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up if player ${player.name} (${player.uniqueId}) has $amount money")
        val user = StickyPlugin.instance.accountManager.getAccount(player.uniqueId)
        if (user != null) return user.hasEnough(amount)
        return false
    }

    override fun has(playerName: String, worldName: String?, amount: Double): Boolean = has(playerName, amount)

    override fun has(player: OfflinePlayer, worldName: String?, amount: Double): Boolean = has(player, amount)

    override fun hasAccount(playerName: String): Boolean {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up if player $playerName exists")
        return StickyPlugin.instance.accountManager.getAccount(playerName) != null
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up if player ${player.name} (${player.uniqueId}) exists")
        return StickyPlugin.instance.accountManager.getAccount(player.uniqueId) != null
    }

    override fun hasAccount(playerName: String, worldName: String?): Boolean = hasAccount(playerName)

    override fun hasAccount(player: OfflinePlayer, worldName: String?): Boolean = hasAccount(player)

    override fun getBalance(playerName: String): Double {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up players' $playerName balance")

        val acc = StickyPlugin.instance.accountManager.getAccount(playerName)
        val currency = StickyPlugin.instance.currencyManager.getDefaultCurrency()

        return currency?.let { acc?.getBalance(it) } ?: 0.0
    }

    override fun getBalance(player: OfflinePlayer): Double {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Looking up players' ${player.name} (${player.uniqueId}) balance")

        val acc = StickyPlugin.instance.accountManager.getAccount(player.uniqueId)
        val currency = StickyPlugin.instance.currencyManager.getDefaultCurrency()

        return currency?.let { acc?.getBalance(it) } ?: 0.0
    }

    override fun getBalance(playerName: String, world: String?): Double = getBalance(playerName)

    override fun getBalance(player: OfflinePlayer, world: String?): Double = getBalance(player)

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Parsing withdraw for player ${player.name} (${player.uniqueId}), need to remove: $amount")
        return sharedWithdrawPlayer(player.uniqueId.toString(), amount, player.name!!, true)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Parsing withdraw for player $playerName, need to remove: $amount")
        return sharedWithdrawPlayer(playerName, amount, playerName)
    }

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String?, amount: Double): EconomyResponse = withdrawPlayer(player, amount)

    override fun withdrawPlayer(playerName: String, worldName: String?, amount: Double): EconomyResponse = withdrawPlayer(playerName, amount)

    private fun sharedWithdrawPlayer(input: String, amount: Double, playerName: String, uuid: Boolean = false): EconomyResponse {
        if (amount < 0.0)
            return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.FAILURE,
            "Cannot withdraw negative funds"
        )

        var balance by Delegates.notNull<Double>()
        var type = EconomyResponse.ResponseType.FAILURE
        var error: String? = null

        val user = if (uuid) {
            StickyPlugin.instance.accountManager.getAccount(UUID.fromString(input))
        } else {
            StickyPlugin.instance.accountManager.getAccount(input)
        }

        val currency = StickyPlugin.instance.currencyManager.getDefaultCurrency()

        if (currency != null && user != null) {
            if (user.withdraw(currency, amount)) {
                type = EconomyResponse.ResponseType.SUCCESS
            } else {
                error = "Could not withdraw $amount from $playerName because they don't have enough funds"
            }
            balance = user.getBalance(currency)
        } else {
            balance = 0.0
            error = "Could not withdraw $amount from $playerName because either the account or currency couldn't be found"
        }

        return EconomyResponse(amount, balance, type, error)
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Parsing deposit for player ${player.name} (${player.uniqueId}), need to add: $amount")
        return sharedDepositPlayer(player.uniqueId.toString(), amount, player.name!!, true)
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        if (StickyPlugin.instance.loggingTransactions) ServerUtils.log("Parsing deposit for player $playerName, need to add: $amount")
        return sharedDepositPlayer(playerName, amount, playerName)
    }

    override fun depositPlayer(player: OfflinePlayer, worldName: String?, amount: Double): EconomyResponse = depositPlayer(player, amount)

    override fun depositPlayer(playerName: String, worldName: String?, amount: Double): EconomyResponse = depositPlayer(playerName, amount)

    private fun sharedDepositPlayer(input: String, amount: Double, playerName: String, uuid: Boolean = false): EconomyResponse {
        if (amount < 0.0)
            return EconomyResponse(
                0.0,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                "Cannot deposit negative funds"
            )

        var balance by Delegates.notNull<Double>()
        var type = EconomyResponse.ResponseType.FAILURE
        var error: String? = null

        val user = if (uuid) {
            StickyPlugin.instance.accountManager.getAccount(UUID.fromString(input))
        } else {
            StickyPlugin.instance.accountManager.getAccount(input)
        }

        val currency = StickyPlugin.instance.currencyManager.getDefaultCurrency()

        if (currency != null && user != null) {
            if (user.deposit(currency, amount)) {
                type = EconomyResponse.ResponseType.SUCCESS
            } else {
                error = "Could not deposit $amount to $playerName because they are not allowed to receive currencies."
            }
            balance = user.getBalance(currency)
        } else {
            balance = 0.0
            error = "Could not withdraw $amount to $playerName because either the account or currency couldn't be found."
        }

        return EconomyResponse(amount, balance, type, error)
    }
}
