package stickyWallet.commands

import StickyPlugin
import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.entity.Player
import stickyWallet.accounts.Account
import stickyWallet.files.L
import stickyWallet.utils.Permissions

class BalanceCommand : TabExecutor {

    private val plugin = StickyPlugin.instance

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyPlugin.doAsync(Runnable {
            if (!sender.hasPermission(Permissions.COMMAND_BALANCE)) {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }

            val account: Account? = if (args.isEmpty() && sender is Player) {
                plugin.accountManager.getAccount(sender)

            } else if (sender.hasPermission(Permissions.COMMAND_BALANCE_OTHER) && args.size == 1) {
                plugin.accountManager.getAccount(args[0])
            } else {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }

            if (account == null) {
                sender.sendMessage(L.playerDoesNotExist)
                return@Runnable
            }

            when (plugin.currencyManager.currencies.size) {
                0 -> sender.sendMessage(L.noDefaultCurrency)
                1 -> {
                    val currency = plugin.currencyManager.getDefaultCurrency()
                    if (currency == null) {
                        sender.sendMessage(L.noBalance.replace("{player}", account.nickname!!))
                    } else {
                        sender.sendMessage(
                            L.balance.replace("{player}", account.displayName)
                                .replace("{currencycolor}", currency.color.toString())
                                .replace("{balance}", currency.format(account.getBalance(currency)))
                        )
                    }
                }
                else -> {
                    sender.sendMessage(L.multipleBalance.replace("{player}", account.displayName))
                    plugin.currencyManager.currencies.forEach {
                        val balance = account.getBalance(it)
                        sender.sendMessage(
                            L.balanceList.replace("{currencycolor}", it.color.toString())
                                .replace("{format}", it.format(balance))
                        )
                    }
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
        if (args.size == 1)
            return Bukkit.getOnlinePlayers().map {
                it.name
            }.filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        return mutableListOf()
    }

}