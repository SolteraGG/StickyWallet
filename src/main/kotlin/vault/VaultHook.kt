package vault

import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse

class VaultHook : AbstractEconomy() {

    override fun getBanks(): MutableList<String> {
        return ArrayList()
    }

    override fun getBalance(playerName: String?): Double {
        TODO("Not yet implemented")
    }

    override fun getBalance(playerName: String?, world: String?): Double {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return "DDD Pawllet"
    }

    override fun isBankOwner(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun has(playerName: String?, amount: Double): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
        TODO("Not yet implemented")
    }

    override fun bankDeposit(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun bankWithdraw(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun deleteBank(name: String?): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun createBank(name: String?, player: String?): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun hasAccount(playerName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAccount(playerName: String?, worldName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isBankMember(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun createPlayerAccount(playerName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun currencyNameSingular(): String {
        val currency = StickyWallet.instance.currencyManager.getDefaultCurrency() ?: return ""
        return currency.singular
    }

    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankHas(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun currencyNamePlural(): String {
        val currency = StickyWallet.instance.currencyManager.getDefaultCurrency() ?: return ""
        return currency.plural
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    override fun bankBalance(name: String?): EconomyResponse {
        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.NOT_IMPLEMENTED,
            "StickyWallet does not support bank accounts"
        )
    }

    override fun format(amount: Double): String {
        val currency = StickyWallet.instance.currencyManager.getDefaultCurrency() ?: return amount.toString()
        return currency.format(amount)
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

}