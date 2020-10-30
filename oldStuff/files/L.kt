package stickyWallet.files

import org.bukkit.command.CommandSender
import stickyWallet.StickyPlugin
import stickyWallet.utils.StringUtils

object L {

    private const val path = "messages"
    private val plugin = StickyPlugin.instance
    private val config = plugin.config

    private fun get(path: String) = config.getString(path)!! // todo: colorize

    // private fun getList(path: String) = config.getStringList(path).map { StringUtils.colorize(it) }

    val noConsole
        get() = "$prefix${get("$path.noConsole")}"

    val insufficientFunds
        get() = "$prefix${get("$path.insufficientFunds")}"

    val targetInsufficientFunds
        get() = "$prefix${get("$path.targetInsufficientFunds")}"

    val payerMessage
        get() = "$prefix${get("$path.payer")}"

    val paidMessage
        get() = "$prefix${get("$path.paid")}"

    val payUsage
        get() = "$prefix${get("$path.usage.pay_command")}"

    val takeMessage
        get() = "$prefix${get("$path.take")}"

    val setMessage
        get() = "$prefix${get("$path.set")}"

    val payYourself
        get() = "$prefix${get("$path.pay_yourself")}"

    val unknownCurrency
        get() = "$prefix${get("$path.unknownCurrency")}"

    val unknownSubCommand
        get() = "$prefix${get("$path.unknownCommand")}"

    fun exchangeHelp(sender: CommandSender) =
            config.getStringList("$path.help.exchange_command")
                    // .forEach { sender.sendMessage(StringUtils.colorize(it.replace("{prefix}", prefix))) }

    val balance
        get() = "$prefix${get("$path.balance.current")}"

    val multipleBalance
        get() = "$prefix${get("$path.balance.multiple")}"

    val balanceList
        get() = "$prefix${get("$path.balance.list")}"

    val invalidAmount
        get() = "$prefix${get("$path.invalidAmount")}"

    val invalidPage
        get() = "$prefix${get("$path.invalidPage")}"

    fun checkHelp(sender: CommandSender) =
            config.getStringList("$path.help.check_command")
                    // .forEach { sender.sendMessage(StringUtils.colorize(it.replace("{prefix}", prefix))) }

    val checkSuccess
        get() = "$prefix${get("$path.check.success")}"

    val checkRedeemed
        get() = "$prefix${get("$path.check.redeemed")}"

    val checkInvalid
        get() = "$prefix${get("$path.check.invalid")}"

    val giveUsage
        get() = "$prefix${get("$path.usage.give_command")}"

    val takeUsage
        get() = "$prefix${get("$path.usage.take_command")}"

    val setUsage
        get() = "$prefix${get("$path.usage.set_command")}"


    val noDefaultCurrency
        get() = "$prefix${get("$path.noDefaultCurrency")}"

    val noBalance
        get() = "$prefix${get("$path.balance.none")}"

    val noPermsToPay
        get() = "$prefix${get("$path.payNoPermission")}"

    val currencyNotPayable
        get() = "$prefix${get("$path.currencyNotPayable")}"

    val accountMissing
        get() = "$prefix${get("$path.accountMissing")}"

    val cannotReceive
        get() = "$prefix${get("$path.cannotReceiveMoney")}"

    object CurrencyUsage {
        val create
            get() = "$prefix${get("$path.usage.currency_create")}"

        val delete
            get() = "$prefix${get("$path.usage.currency_delete")}"

        val view
            get() = "$prefix${get("$path.usage.currency_view")}"

        val default
            get() = "$prefix${get("$path.usage.currency_default")}"

        val list
            get() = "$prefix${get("$path.usage.currency_list")}"

        val color
            get() = "$prefix${get("$path.usage.currency_color")}"

        val colorList
            get() = "$prefix${get("$path.usage.currency_colorlist")}"

        val payable
            get() = "$prefix${get("$path.usage.currency_payable")}"

        val startBal
            get() = "$prefix${get("$path.usage.currency_startbal")}"

        val decimals
            get() = "$prefix${get("$path.usage.currency_decimals")}"

        val symbol
            get() = "$prefix${get("$path.usage.currency_symbol")}"

        val rate
            get() = "$prefix${get("$path.usage.currency_rate")}"

        val backend
            get() = "$prefix${get("$path.usage.currency_backend")}"

        val convert
            get() = "$prefix${get("$path.usage.currency_convert")}"

    }

    val exchangeSuccess
        get() = "$prefix${get("$path.exchange_success")}"

    val exchangeSuccessCustom
        get() = "$prefix${get("$path.exchange_success_custom")}"

    val exchangeSuccessCustomOther
        get() = "$prefix${get("$path.exchange_success_custom_other")}"


    val exchangeNoPermsCustom
        get() = "$prefix${get("$path.exchange_command.no_perms.custom")}"

    val exchangeNoPermsPreset
        get() = "$prefix${get("$path.exchange_command.no_perms.preset")}"
}
