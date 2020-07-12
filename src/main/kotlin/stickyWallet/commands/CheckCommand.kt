package stickyWallet.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import stickyWallet.accounts.ConsoleAccount
import stickyWallet.check.CheckManager
import stickyWallet.configs.L
import stickyWallet.configs.PluginConfiguration.CheckSettings
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.Permissions
import stickyWallet.utils.StringUtilities
import stickyWallet.utils.StringUtilities.colorize
import java.math.BigDecimal

class CheckCommand : TabExecutor, UsePlugin {
    private val possibleArguments = listOf("redeem", "write")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(Permissions.COMMAND_CHECK)) {
            sender.sendMessage(L.noPermissions)
            return true
        }

        if (args.isEmpty()) {
            L.Check.sendUsage(sender)
            return true
        }

        when (args.size) {
            1 -> {
                if (sender !is Player) {
                    sender.sendMessage(L.noConsole)
                    return true
                }

                if (!args[0].equals("redeem", true)) {
                    sender.sendMessage(L.unknownSubCommand)
                    return true
                }

                val itemInHand = sender.inventory.itemInMainHand
                val itemInOffhand = sender.inventory.itemInOffHand

                if (itemInHand.type != CheckSettings.material && itemInOffhand.type != CheckSettings.material) {
                    sender.sendMessage(L.Check.invalid)
                    return true
                }

                val finalBukkitItem = if (itemInHand.type == CheckSettings.material) {
                    itemInHand
                } else {
                    itemInOffhand
                }

                val finalItem: CheckManager.CheckData? = when {
                    itemInHand.type == CheckSettings.material -> CheckManager.validateCheck(itemInHand)
                        ?: if (itemInOffhand.type == CheckSettings.material) {
                            val valid2 = CheckManager.validateCheck(itemInOffhand)
                            valid2
                        } else null
                    itemInOffhand.type == CheckSettings.material -> CheckManager.validateCheck(itemInOffhand)
                    else -> null
                }

                if (finalItem == null) {
                    sender.sendMessage(L.Check.invalid)
                    return true
                }

                val (value, currency) = finalItem

                val userAccount = pluginInstance.accountStore.getAccount(sender)
                val finalCurrency = CheckManager.getCurrencyForCheck(currency)

                // Sanity checks
                if (userAccount == null) {
                    sender.sendMessage(L.playerDoesNotExist)
                    return true
                }

                if (finalCurrency == null) {
                    sender.sendMessage(L.Check.invalid)
                    return true
                }

                if (finalBukkitItem.amount > 1) {
                    sender.inventory.itemInMainHand.amount = finalBukkitItem.amount - 1
                } else {
                    sender.inventory.remove(finalBukkitItem)
                }

                userAccount.deposit(finalCurrency, value)
                sender.sendMessage(L.Check.redeemed)
                return true
            }
            2, 3, 4 -> {
                if (!args[0].equals("write", true)) {
                    sender.sendMessage(L.unknownSubCommand)
                    return true
                }

                val (_, rawAmount) = args

                if (!StringUtilities.validateInput(sender, rawAmount)) {
                    return true
                }

                var noDefault = false
                val currency = if (args.size == 3) {
                    pluginInstance.currencyStore.getCurrency(args[2])
                } else {
                    val temp = pluginInstance.currencyStore.getDefaultCurrency()
                    if (temp == null) noDefault = true
                    temp
                }

                if (currency == null) {
                    sender.sendMessage(if (noDefault) {
                        L.noDefaultCurrency
                    } else {
                        L.unknownCurrency
                    })
                    return true
                }

                val amount = try {
                    val temp = if (currency.decimalSupported) {
                        rawAmount.toBigDecimal()
                    } else {
                        rawAmount.toBigDecimal().toBigInteger().toBigDecimal()
                    }
                    if (temp < BigDecimal.ZERO) throw NumberFormatException()
                    temp
                } catch (ex: NumberFormatException) {
                    sender.sendMessage(L.invalidAmount)
                    BigDecimal(-111.111)
                }

                if (amount == BigDecimal(-111.111) || amount == BigDecimal.ZERO) {
                    sender.sendMessage(L.invalidAmount)
                    return true
                }

                val user = if (sender is Player) {
                    pluginInstance.accountStore.getAccount(sender)
                } else {
                    ConsoleAccount()
                }

                // Sanity check
                if (user == null) {
                    sender.sendMessage(L.playerDoesNotExist)
                    return true
                }

                if (user.hasEnough(amount)) {
                    val item = CheckManager.write(if (sender is Player) {
                        sender.name
                    } else {
                        "console"
                    }, currency, amount)
                    user.withdraw(currency, amount)

                    if (sender !is Player) {
                        val player = Bukkit.getOnlinePlayers().find { it.name == args[3] }

                        if (player == null) {
                            sender.sendMessage(L.Check.psstError)
                            return true
                        }

                        player.sendActionBar(colorize("&4Hmm...you notice a mysterious item in your inventory.. What could it be?"))
                        if (player.inventory.addItem(item).isNotEmpty()) {
                            player.world.dropItemNaturally(player.location, item)
                        }
                        sender.sendMessage(L.Check.psstOk)
                    } else {
                        if (sender.inventory.addItem(item).isNotEmpty()) {
                            sender.world.dropItemNaturally(sender.location, item)
                        }
                    }
                    sender.sendMessage(L.Check.success)
                } else {
                    sender.sendMessage(
                        L.Pay.insufficientFunds
                            .replace("{currencycolor}", currency.color.toString())
                            .replace("{currency}", currency.singular)
                    )
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
        if (args.size == 1) {
            return possibleArguments.filter {
                it.startsWith(args[0], true)
            }.toMutableList()
        }

        // Currencies
        if (args.size == 3 && args[0].equals("write", true)) {
            val currencyKeys = pluginInstance.currencyStore.currencies.map { it.singular }.toMutableList()
            currencyKeys.addAll(pluginInstance.currencyStore.currencies.map { it.plural })

            return currencyKeys.filter {
                it.startsWith(args[2], true)
            }.toMutableList()
        }

        if (args.size == 4 && args[0].equals("write", true) && (sender.isOp || sender !is Player)) {
            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .toMutableList()
        }

        return mutableListOf()
    }
}
