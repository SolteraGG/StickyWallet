package stickyWallet.utils

object Permissions {

    const val COMMAND_CURRENCY = "stickywallet.command.currency"
    const val COMMAND_BALANCE = "stickywallet.command.balance"
    const val COMMAND_BALANCE_OTHER = "stickywallet.command.balance.other"
    const val COMMAND_CHECK = "stickywallet.command.check"
    const val COMMAND_ECONOMY = "stickywallet.command.economy"
    const val COMMAND_PAY = "stickywallet.command.pay"
    const val COMMAND_BALANCE_TOP = "stickywallet.command.balancetop"

    const val COMMAND_ECONOMY_GIVE = "$COMMAND_ECONOMY.give"
    const val COMMAND_ECONOMY_TAKE = "$COMMAND_ECONOMY.take"
    const val COMMAND_ECONOMY_SET = "$COMMAND_ECONOMY.set"

    fun payCommandCurrency(name: String) = "$COMMAND_PAY.$name"

}