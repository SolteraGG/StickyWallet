package stickyWallet.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.StickyPlugin
import stickyWallet.files.L
import stickyWallet.utils.Permissions

class BalanceTopCommand : TabExecutor {

    private val plugin = StickyPlugin.instance
    private val accountsPerPage = 10

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyPlugin.doAsync(Runnable {
            if (!sender.hasPermission(Permissions.COMMAND_BALANCE_TOP)) {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }

            val dataStore = plugin.dataStore

            if (!dataStore.isFetchingTopSupported) {
                sender.sendMessage(
                    L.balanceTopNoSupport
                        .replace("{storage}", dataStore.name)
                )
                return@Runnable
            }

            var currency = plugin.currencyManager.getDefaultCurrency()
            var page = 1

            if (args.isNotEmpty()) {
                val (currencyOrPage) = args
                try {
                    page = currencyOrPage.toInt()
                } catch (_: Exception) {
                    currency = plugin.currencyManager.getCurrency(currencyOrPage)
                }

                if (args.size == 2) {
                    try {
                        page = args[1].toInt()
                    } catch (_: Exception) {
                        sender.sendMessage(L.invalidPage)
                        return@Runnable
                    }
                }
            }

            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@Runnable
            }

            if (page < 1) page = 1

            val offset = 10 * (page - 1)

            val topList = dataStore.getTopList(currency, offset, accountsPerPage)!!

            sender.sendMessage(
                L.balanceTopHeader
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{currencyplural}", currency.plural)
                    .replace("{page}", page.toString())
            )

            if (topList.isEmpty()) {
                sender.sendMessage(L.balanceTopEmpty)
                return@Runnable
            }

            var num = (10 * (page - 1)) + 1

            topList.forEach { (name, balance) ->
                sender.sendMessage(
                    L.balanceTop
                        .replace("{number}", num.toString())
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{player}", name)
                        .replace("{balance}", currency.format(balance))
                )
                num++
            }
            sender.sendMessage(
                L.balanceTopNext
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{currencyplural}", currency.plural)
                    .replace("{page}", (page + 1).toString())
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
        if (args.size == 1) {
            val currencyKeys = plugin.currencyManager.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(plugin.currencyManager.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[0], true)
            }.toMutableList()
        }

        return mutableListOf()
    }

}