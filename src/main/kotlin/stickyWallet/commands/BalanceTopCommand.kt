package stickyWallet.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.StickyWallet
import stickyWallet.configs.L
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions

class BalanceTopCommand : TabExecutor, UsePlugin {
    private val accountsPerPage = 10

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyWallet.doAsync {
            if (!sender.hasPermission(Permissions.COMMAND_BALANCE_TOP)) {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }

            val dataStore = pluginInstance.dataHandler
            val currencyStore = pluginInstance.currencyStore

            var currency = currencyStore.getDefaultCurrency()
            var page = 1L

            if (args.isNotEmpty()) {
                val (currencyOrPage) = args
                try {
                    page = currencyOrPage.toLong()
                } catch (_: Exception) {
                    currency = currencyStore.getCurrency(currencyOrPage)
                }

                if (args.size == 2) {
                    try {
                        page = args[1].toLong()
                    } catch (_: Exception) {
                        sender.sendMessage(L.invalidPage)
                        return@doAsync
                    }
                }
            }

            if (currency == null) {
                sender.sendMessage(L.unknownCurrency)
                return@doAsync
            }

            if (page < 1) page = 1

            val offset = 10 * (page - 1)

            val topList = dataStore.getTopList(currency, offset, accountsPerPage)!!

            sender.sendMessage(
                L.BalTop.header
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{currencyplural}", currency.plural)
                    .replace("{page}", page.toString())
            )

            if (topList.isEmpty()) {
                sender.sendMessage(L.BalTop.empty)
                return@doAsync
            }

            var num = (10 * (page - 1)) + 1

            topList.forEach { (name, balance) ->
                sender.sendMessage(
                    L.BalTop.entry
                        .replace("{number}", num.toString())
                        .replace("{currencycolor}", currency.color.toString())
                        .replace("{player}", name)
                        .replace("{balance}", currency.format(balance))
                )
                num++
            }
            sender.sendMessage(
                L.BalTop.next
                    .replace("{currencycolor}", currency.color.toString())
                    .replace("{currencyplural}", currency.plural)
                    .replace("{page}", (page + 1).toString())
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
        if (args.size == 1) {
            val currencyKeys = pluginInstance.currencyStore.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(pluginInstance.currencyStore.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[0], true)
            }.toMutableList()
        }

        return mutableListOf()
    }
}
