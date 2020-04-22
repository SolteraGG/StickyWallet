package stickyWallet.commands

import StickyPlugin
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import stickyWallet.check.CheckManager
import stickyWallet.files.L
import stickyWallet.nbt.NBTItem
import stickyWallet.utils.Permissions
import stickyWallet.utils.StringUtils

class CheckCommand : TabExecutor {

    private val plugin = StickyPlugin.instance
    private val possibleArguments = listOf("redeem", "write")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(L.noConsole)
            return true
        }

        if (!sender.hasPermission(Permissions.COMMAND_CHECK)) {
           sender.sendMessage(L.noPermissions)
            return true
        }

        if (args.isEmpty()) {
            L.checkHelp(sender)
            return true
        }

        when (args.size) {
            1 -> {
                if (!args[0].equals("redeem", true)) {
                    sender.sendMessage(L.unknownSubCommand)
                    return true
                }

                val itemInHand = sender.inventory.itemInMainHand

                if (itemInHand.type != Material.valueOf(plugin.config.getString("check.material")!!.toUpperCase())) {
                    sender.sendMessage(L.checkInvalid)
                    return true
                }

                val item = NBTItem(itemInHand)
                val checkValue = item.getString(CheckManager.nbtValue)
                val checkCurrency = item.getString(CheckManager.nbtCurrency)

                if (item.bukkitItem.itemMeta.hasDisplayName() && item.bukkitItem.itemMeta.hasLore() && checkValue == null && checkCurrency == null) {
                    sender.sendMessage(L.checkInvalid)
                    return true
                }

                if (!plugin.checkManager.isValid(item)) {
                    sender.sendMessage(L.checkInvalid)
                    return true
                }

                if (checkValue == null && checkCurrency == null) {
                    sender.sendMessage(L.checkInvalid)
                    return true
                }

                val amount = checkValue!!.toDouble()
                val userAccount = plugin.accountManager.getAccount(sender)
                val currency = plugin.checkManager.getCurrencyForCheck(item)

                // Sanity checks
                if (userAccount == null) {
                    sender.sendMessage(L.playerDoesNotExist)
                    return true
                }

                if (currency == null) {
                    sender.sendMessage(L.checkInvalid)
                    return true
                }

                if (item.bukkitItem.amount > 1) {
                    sender.inventory.itemInMainHand.amount = item.bukkitItem.amount - 1
                } else {
                    sender.inventory.remove(item.bukkitItem)
                }

                userAccount.deposit(currency, amount)
                sender.sendMessage(L.checkRedeemed)
                return true
            }
            2, 3 -> {
                if (!args[0].equals("write", true)) {
                    sender.sendMessage(L.unknownSubCommand)
                    return true
                }

                val (_, rawAmount) = args

                if (!StringUtils.validateInput(sender, rawAmount)) {
                    sender.sendMessage(L.invalidAmount)
                    return true
                }

                val amount = rawAmount.toDouble()
                if (amount == 0.0) {
                    sender.sendMessage(L.invalidAmount)
                    return true
                }

                var noDefault = false
                val currency = if (args.size == 3) {
                    plugin.currencyManager.getCurrency(args[2])
                } else {
                    val temp = plugin.currencyManager.getDefaultCurrency()
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

                val user = plugin.accountManager.getAccount(sender)
                // Sanity check
                if (user == null) {
                    sender.sendMessage(L.playerDoesNotExist)
                    return true
                }

                if (user.hasEnough(amount)) {
                    val item = plugin.checkManager.write(sender.name, currency, amount)
                    if (item == null) {
                        sender.sendMessage(L.currencyNotPayable)
                        return true
                    }

                    user.withdraw(currency, amount)
                    if (sender.inventory.addItem(item).isNotEmpty()) {
                        sender.world.dropItemNaturally(sender.location, item)
                    }
                    sender.sendMessage(L.checkSuccess)
                } else {
                    sender.sendMessage(
                        L.insufficientFunds.replace("{currencycolor}", currency.color.toString())
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

        return mutableListOf()
    }

}