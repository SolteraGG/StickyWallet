package stickyWallet.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import stickyWallet.StickyWallet
import stickyWallet.configs.L
import stickyWallet.currencies.Currency
import stickyWallet.events.PayEvent
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions
import java.math.BigDecimal

class PayCommand : TabExecutor, UsePlugin {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyWallet.doAsync {
            if (sender !is Player) {
                sender.sendMessage(L.noConsole)
                return@doAsync
            }
            if (!sender.hasPermission(Permissions.COMMAND_PAY)) {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }

            if (args.size < 2) {
                sender.sendMessage(L.Pay.usage)
                return@doAsync
            }

            val (accountSearch, rawAmount) = args

            val playerAccount = pluginInstance.accountStore.getAccount(sender)
            if (playerAccount == null) {
                sender.sendMessage(L.accountMissing)
                return@doAsync
            }

            var currency = pluginInstance.currencyStore.getDefaultCurrency()
            if (currency == null) {
                sender.sendMessage(L.noDefaultCurrency)
                return@doAsync
            }

            if (args.size > 2)
                currency = pluginInstance.currencyStore.getCurrency(args[2])
            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@doAsync
            }

            if (
                !sender.hasPermission(Permissions.payCommandCurrency(currency.singular)) ||
                !sender.hasPermission(Permissions.payCommandCurrency(currency.plural))
            ) {
                sender.sendMessage(
                    L.Pay.noPerms
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{currency}", currency.plural)
                )
                return@doAsync
            }

            val amount = parseAmount(currency, rawAmount)
            if (amount == BigDecimal(-111.111)) {
                sender.sendMessage(L.invalidAmount)
                return@doAsync
            }

            val receiverAccount = pluginInstance.accountStore.getAccount(accountSearch)
            if (receiverAccount == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@doAsync
            }

            if (playerAccount.uuid == receiverAccount.uuid) {
                sender.sendMessage(L.Pay.yourself)
                return@doAsync
            }

            if (!playerAccount.hasEnough(currency, amount)) {
                sender.sendMessage(
                    L.Pay.insufficientFunds
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{currency}", currency.plural)
                )
                return@doAsync
            }

            val event = PayEvent(currency, playerAccount, receiverAccount, amount)

            StickyWallet.doSync {
                Bukkit.getPluginManager().callEvent(event)
            }

            if (event.isCancelled) {
                sender.sendMessage("${L.prefix}${ChatColor.RED}Pay event was canceled!")
                return@doAsync
            }

            val accountBal = playerAccount.getBalanceForCurrency(currency) - amount
            val receiverBal = receiverAccount.getBalanceForCurrency(currency) + amount

            playerAccount.modifyCurrencyBalance(currency, accountBal, true)
            receiverAccount.modifyCurrencyBalance(currency, receiverBal, true)
            pluginInstance.economyLogger.info(
                """
                [PAYMENT]
                ${playerAccount.displayName} paid ${receiverAccount.displayName} ${currency.format(amount)}
                New Balances:
                  ${playerAccount.displayName} -> ${currency.format(accountBal)}
                  ${receiverAccount.displayName} -> ${currency.format(receiverBal)}
            """.trimIndent()
            )

            Bukkit.getPlayer(receiverAccount.uuid)?.sendMessage(
                L.Pay.paid
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{amount}", currency.format(amount))
                    .replace("{player}", sender.name)
            )

            sender.sendMessage(
                L.Pay.payer
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{amount}", currency.format(amount))
                    .replace("{player}", receiverAccount.displayName)
            )
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // Account
        if (args.size == 1)
            return Bukkit.getOnlinePlayers().map {
                it.name
            }.filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        // ?Currency
        if (args.size == 3) {
            val currencyKeys = pluginInstance.currencyStore.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(pluginInstance.currencyStore.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[2], true)
            }.toMutableList()
        }

        return mutableListOf()
    }

    private fun parseAmount(currency: Currency, amount: String) = try {
        val temp = if (currency.decimalSupported) {
            amount.toBigDecimal()
        } else {
            amount.toBigDecimal().toBigInteger().toBigDecimal()
        }
        if (temp < BigDecimal.ZERO) throw NumberFormatException()
        temp
    } catch (ex: NumberFormatException) {
        BigDecimal(-111.111)
    }
}
