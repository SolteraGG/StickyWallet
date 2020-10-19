package stickyWallet.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import stickyWallet.StickyWallet
import stickyWallet.accounts.Account
import stickyWallet.configs.L
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions
import stickyWallet.utils.Utilities

class BalanceCommand : TabExecutor, UsePlugin {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyWallet.doAsync {
            if (!sender.hasPermission(Permissions.COMMAND_BALANCE)) {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }

            val account: Account? = if (args.isEmpty() && sender is Player) {
                pluginInstance.accountStore.getAccount(sender)
            } else if (sender.hasPermission(Permissions.COMMAND_BALANCE_OTHER) && args.size == 1) {
                pluginInstance.accountStore.getAccount(args[0])
            } else {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }

            if (account == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@doAsync
            }

            when (pluginInstance.currencyStore.currencies.size) {
                0 -> sender.sendMessage(L.noDefaultCurrency)
                1 -> {
                    val currency = pluginInstance.currencyStore.getDefaultCurrency()
                    if (currency == null) {
                        sender.sendMessage(L.Balance.none.replace("{player}", account.playerName!!))
                    } else {
                        sender.sendMessage(
                            L.Balance.currennt
                                .replace("{player}", account.displayName)
                                .replace("{currencycolor}", currency.color.toString())
                                .replace("{balance}", currency.format(account.getBalanceForCurrency(currency)))
                        )
                    }
                }
                else -> {
                    sender.sendMessage(L.Balance.multipleHeader.replace("{player}", account.displayName))
                    pluginInstance.currencyStore.currencies.forEach {
                        val balance = account.getBalanceForCurrency(it)
                        sender.sendMessage(
                            L.Balance.multipleEntry
                                .replace("{currencycolor}", it.color.toString())
                                .replace("{format}", it.format(balance))
                        )
                    }
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
        // Possible accounts
        if (args.size == 1)
            return Utilities.getPlayerNames().filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        return mutableListOf()
    }
}
