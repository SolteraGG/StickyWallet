package stickyWallet.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.StickyWallet
import stickyWallet.accounts.Account
import stickyWallet.configs.L
import stickyWallet.currencies.Currency
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions
import java.math.BigDecimal

class EconomyCommand : TabExecutor, UsePlugin {
    private val subCommands = listOf(
        "add",
        "give",
        "remove",
        "set",
        "take"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyWallet.doAsync {
            if (!sender.hasPermission(Permissions.COMMAND_ECONOMY)) {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }
            if (args.isEmpty()) {
                L.Economy.sendUsage(sender)
                return@doAsync
            }

            val subCommand: String = args[0]

            if (!subCommands.any { it.equals(subCommand, true) }) {
                sender.sendMessage(L.unknownSubCommand)
                return@doAsync
            }

            when (subCommand) {
                "add", "give" -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_GIVE)) {
                    sender.sendMessage(L.noPermissions)
                    return@doAsync
                }
                "remove", "take" -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_TAKE)) {
                    sender.sendMessage(L.noPermissions)
                    return@doAsync
                }
                else -> if (!sender.hasPermission(Permissions.COMMAND_ECONOMY_SET)) {
                    sender.sendMessage(L.noPermissions)
                    return@doAsync
                }
            }

            val errorMessage = when (subCommand) {
                "add", "give" -> L.Economy.give
                "remove", "take" -> L.Economy.take
                else -> L.Economy.set
            }

            if (args.size < 3) {
                sender.sendMessage(errorMessage)
                return@doAsync
            }

            val account: Account? = pluginInstance.accountStore.getAccount(args[1])
            if (account == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@doAsync
            }

            var currency = pluginInstance.currencyStore.getDefaultCurrency()

            if (args.size > 3)
                currency = pluginInstance.currencyStore.getCurrency(args[3])

            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@doAsync
            }

            val amount = parseAmount(currency, args[2])

            if (amount == BigDecimal(-111.111)) {
                sender.sendMessage(L.invalidAmount)
                return@doAsync
            }

            val returnMessage = when (subCommand) {
                "add", "give" -> L.Economy.addResult
                "remove", "take" -> if (account.hasEnough(currency, amount)) {
                    L.Economy.takeResult
                } else {
                    L.targetInsufficientFunds
                }
                else -> L.Economy.setResult
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
                    account.set(currency, amount)
                    sender.sendMessage(returnMessage)
                }
            }
        }
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
            val currencyKeys = pluginInstance.currencyStore.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(pluginInstance.currencyStore.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[3], true)
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
