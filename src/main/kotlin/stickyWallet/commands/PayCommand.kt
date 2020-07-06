package stickyWallet.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import stickyWallet.StickyPlugin
import stickyWallet.currency.Currency
import stickyWallet.events.PayEvent
import stickyWallet.files.L
import stickyWallet.utils.Permissions

class PayCommand : TabExecutor {

    private val plugin = StickyPlugin.instance

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyPlugin.doAsync(Runnable {
            if (sender !is Player) {
                sender.sendMessage(L.noConsole)
                return@Runnable
            }
            if (!sender.hasPermission(Permissions.COMMAND_PAY)) {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }

            if (args.size < 2) {
                sender.sendMessage(L.payUsage)
                return@Runnable
            }

            val (accountSearch, rawAmount) = args

            val playerAccount = plugin.accountManager.getAccount(sender)
            if (playerAccount == null) {
                sender.sendMessage(L.accountMissing)
                return@Runnable
            }

            var currency = plugin.currencyManager.getDefaultCurrency()
            if (currency == null) {
                sender.sendMessage(L.noDefaultCurrency)
                return@Runnable
            }

            if (args.size > 2)
                currency = plugin.currencyManager.getCurrency(args[2])
            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@Runnable
            }

            if (!currency.payable) {
                sender.sendMessage(
                    L.currencyNotPayable
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{currency}", currency.plural)
                )
                return@Runnable
            }

            if (!sender.hasPermission(Permissions.payCommandCurrency(currency.singular)) || !sender.hasPermission(Permissions.payCommandCurrency(currency.plural))) {
                sender.sendMessage(
                    L.noPermsToPay
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{currency}", currency.plural)
                )
                return@Runnable
            }

            val amount = parseAmount(currency, rawAmount)
            if (amount == -111.111) {
                sender.sendMessage(L.invalidAmount)
                return@Runnable
            }

            val receiverAccount = plugin.accountManager.getAccount(accountSearch)
            if (receiverAccount == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@Runnable
            }

            if (playerAccount.uuid == receiverAccount.uuid) {
                sender.sendMessage(L.payYourself)
                return@Runnable
            }

            if (!receiverAccount.canReceiveCurrency) {
                sender.sendMessage(
                    L.cannotReceive
                        .replace("{player}", receiverAccount.displayName)
                )
                return@Runnable
            }

            if (!playerAccount.hasEnough(currency, amount)) {
                sender.sendMessage(
                    L.insufficientFunds
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{currency}", currency.plural)
                )
                return@Runnable
            }

            val event = PayEvent(currency, playerAccount, receiverAccount, amount)
            StickyPlugin.doSync(Runnable {
                Bukkit.getPluginManager().callEvent(event)
            })
            if (event.isCancelled) {
                sender.sendMessage("${L.prefix}${ChatColor.RED}Pay event was canceled!")
                return@Runnable
            }

            val accountBal = playerAccount.getBalance(currency) - amount
            val receiverBal = receiverAccount.getBalance(currency) + amount

            playerAccount.modifyBalance(currency, accountBal, true)
            receiverAccount.modifyBalance(currency, receiverBal, true)
            plugin.economyLogger.log("""
                [PAYMENT]
                ${playerAccount.displayName} paid ${receiverAccount.displayName} ${currency.format(amount)}
                New Balances:
                ${playerAccount.displayName} -> ${currency.format(accountBal)}
                ${receiverAccount.displayName} -> ${currency.format(receiverBal)}
            """.trimIndent())

            Bukkit.getPlayer(receiverAccount.uuid)?.sendMessage(
                L.paidMessage
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{amount}", currency.format(amount))
                    .replace("{player}", sender.name)
            )

            sender.sendMessage(
                L.payerMessage
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{amount}", currency.format(amount))
                    .replace("{player}", receiverAccount.displayName)
            )
        })
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
            val currencyKeys = plugin.currencyManager.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(plugin.currencyManager.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[2], true)
            }.toMutableList()
        }

        return mutableListOf()
    }

    private fun parseAmount(currency: Currency, amount: String) = try {
        val temp = if (currency.decimalSupported) {
            amount.toDouble()
        } else {
            amount.toInt().toDouble()
        }
        if (temp < 0.0) throw NumberFormatException()
        temp
    } catch (ex: NumberFormatException) {
        -111.111
    }

}