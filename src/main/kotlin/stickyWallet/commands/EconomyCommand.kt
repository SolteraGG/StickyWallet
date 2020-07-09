package stickyWallet.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.StickyPlugin
import stickyWallet.accounts.Account
import stickyWallet.currency.Currency
import stickyWallet.files.L
import stickyWallet.utils.Permissions

class EconomyCommand : TabExecutor {

    private val plugin = StickyPlugin.instance

    private val subCommands = listOf(
        "add",
        "give",
        "remove",
        "set",
        "take"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyPlugin.doAsync(Runnable {
            if (!sender.hasPermission(Permissions.COMMAND_ECONOMY)) {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }
            if (args.isEmpty()) {
                L.manageHelp(sender)
                return@Runnable
            }

            val subCommand: String = args[0]

            if (!subCommands.any { it.equals(subCommand, true) }) {
                sender.sendMessage(L.unknownSubCommand)
                return@Runnable
            }

            when (subCommand) {
                "add", "give" -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_GIVE)) {
                    sender.sendMessage(L.noPermissions)
                    return@Runnable
                }
                "remove", "take" -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_TAKE)) {
                    sender.sendMessage(L.noPermissions)
                    return@Runnable
                }
                else -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_SET)) {
                    sender.sendMessage(L.noPermissions)
                    return@Runnable
                }
            }

            val errorMessage = when (subCommand) {
                "add", "give" -> L.giveUsage
                "remove", "take" -> L.takeUsage
                else -> L.setUsage
            }

            if (args.size < 3) {
                sender.sendMessage(errorMessage)
                return@Runnable
            }

            val account: Account? = plugin.accountManager.getAccount(args[1])
            if (account == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@Runnable
            }

            var currency = plugin.currencyManager.getDefaultCurrency()

            if (args.size > 3)
                currency = plugin.currencyManager.getCurrency(args[3])

            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@Runnable
            }

            val amount = parseAmount(currency, args[2])

            if (amount == -111.111) {
                sender.sendMessage(L.invalidAmount)
                return@Runnable
            }

            val returnMessage = when (subCommand) {
                "add", "give" -> if (account.canReceiveCurrency) {
                    L.addMessage
                } else {
                    L.cannotReceive
                }
                "remove", "take" -> if (account.hasEnough(currency, amount)) {
                    L.takeMessage
                } else {
                    L.targetInsufficientFunds
                }
                else -> L.setMessage
            }
                .replace("{player}", account.displayName)
                .replace("{currencycolor}", currency.color.toString())
                .replace("{amount}", currency.format(amount))
                .replace("{target}", account.displayName)
                .replace("{currency}", currency.plural)

            when (subCommand) {
                "add", "give" -> {
                    account.deposit(currency, amount)
                    sender.sendMessage(returnMessage)
                }
                "take", "remove" -> {
                    account.withdraw(currency, amount)
                    sender.sendMessage(returnMessage)
                }
                else -> {
                    account.setBalance(currency, amount)
                    sender.sendMessage(returnMessage)
                }
            }
        })
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // Sub command
        if (args.size == 1)
            return subCommands.filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        // Account
        if (args.size == 2)
            return Bukkit.getOnlinePlayers().map {
                it.name
            }.filter {
                it.startsWith(args[1], true)
            }.toMutableList()

        // ?Currency
        if (args.size == 4) {
            val currencyKeys = plugin.currencyManager.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(plugin.currencyManager.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[3], true)
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
