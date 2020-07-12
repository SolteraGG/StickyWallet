package stickyWallet.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.StickyWallet
import stickyWallet.configs.L
import stickyWallet.configs.PluginConfiguration
import stickyWallet.interfaces.UsePlugin
import stickyWallet.utils.NumberUtilities
import stickyWallet.utils.Permissions
import stickyWallet.utils.StringUtilities
import java.math.BigDecimal

class CurrencyCommand : TabExecutor, UsePlugin {
    private val subCommands = listOf(
        "color",
        "colorlist",
        "create",
        "decimals",
        "default",
        "delete",
        "list",
        "setrate",
        "startbal",
        "symbol",
        "view"
    )

    private val needsCurrency = listOf(
        "color",
        "decimals",
        "default",
        "delete",
        "setrate",
        "startbal",
        "symbol",
        "view"
    )

    private val possibleColors = listOf(
        "black",
        "dark_blue",
        "dark_green",
        "dark_aqua",
        "dark_red",
        "dark_purple",
        "gold",
        "gray",
        "dark_gray",
        "blue",
        "green",
        "aqua",
        "red",
        "light_purple",
        "yellow",
        "white",
        "reset"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        StickyWallet.doAsync {
            if (!sender.hasPermission(Permissions.COMMAND_CURRENCY)) {
                sender.sendMessage(L.noPermissions)
                return@doAsync
            }
            if (args.isEmpty()) {
                L.Currency.sendUsage(sender)
                return@doAsync
            }

            val (stringCommand) = args

            when (stringCommand.toLowerCase()) {
                "create" -> {
                    if (args.size != 4) {
                        sender.sendMessage(L.Currency.create)
                        return@doAsync
                    }
                    val (_, singular, plural, type) = args
                    createCurrency(sender, singular, plural, type)
                }
                "list" -> {
                    val currencies = pluginInstance.currencyStore.currencies
                    val builder = StringBuilder().append(L.prefix).append("§7There ")
                        .append(StringUtilities.pluralize(currencies.size, "is", "are"))
                        .append(" §f${currencies.size}§7 ")
                        .append(StringUtilities.pluralize(currencies.size, "currency", "currencies"))
                    sender.sendMessage(builder.toString())
                    currencies.forEach {
                        sender.sendMessage("§a§l>> §e${it.plural}")
                    }
                }
                "view" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.Currency.view)
                        return@doAsync
                    }
                    val (_, currency) = args
                    listCurrency(sender, currency)
                }
                "startbal" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.Currency.startBal)
                        return@doAsync
                    }
                    val (_, currency, newStart) = args
                    setNewStartBal(sender, currency, newStart)
                }
                "color" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.Currency.color)
                        return@doAsync
                    }
                    val (_, currencySearch, newColor) = args
                    if (!possibleColors.any { it.equals(newColor, true) }) {
                        sender.sendMessage("${L.prefix}§cInvalid chat color. Use the autocomplete feature, please")
                        return@doAsync
                    }
                    val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@doAsync
                    }

                    val color = ChatColor.valueOf(newColor.toUpperCase())
                    currency.color = color
                    pluginInstance.dataHandler.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7The color for §f${currency.plural} §7was set to: $color${color.name}")
                }
                "colorlist" -> {
                    possibleColors.map { color ->
                        if (color == "reset") {
                            "${ChatColor.WHITE}§l${color.capitalize()} §7= $color"
                        } else {
                            "${ChatColor.valueOf(color.toUpperCase())}§l${
                                color.split("_").joinToString(" ") {
                                    it.capitalize()
                                }
                            } §7= $color"
                        }
                    }
                        .forEach {
                            sender.sendMessage(it)
                        }
                }
                "symbol" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.Currency.symbol)
                        return@doAsync
                    }
                    val (_, currencySearch, resetOrNewSymbol) = args
                    val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@doAsync
                    }

                    val newSymbol: String?

                    when {
                        resetOrNewSymbol.equals("remove", true) -> {
                            newSymbol = null
                            sender.sendMessage("${L.prefix}§7Currency symbol removed for §f${currency.plural}")
                        }
                        resetOrNewSymbol.length == 1 -> {
                            newSymbol = resetOrNewSymbol
                            sender.sendMessage("${L.prefix}§7Currency symbol for §f${currency.plural} §7was updated to: §a$resetOrNewSymbol")
                        }
                        else -> {
                            sender.sendMessage("${L.prefix}§7Symbol must be 1 character or removed with \"${ChatColor.AQUA}remove§7\"")
                            return@doAsync
                        }
                    }

                    currency.symbol = newSymbol
                    pluginInstance.dataHandler.saveCurrency(currency)
                }
                "default" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.Currency.default)
                        return@doAsync
                    }
                    val (_, currencySearch) = args
                    val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@doAsync
                    }

                    val defaultCurrency = pluginInstance.currencyStore.getDefaultCurrency()
                    if (defaultCurrency != null && defaultCurrency != currency) {
                        defaultCurrency.defaultCurrency = false
                        pluginInstance.dataHandler.saveCurrency(defaultCurrency)
                    }

                    currency.defaultCurrency = true
                    pluginInstance.dataHandler.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7Set default currency to §f${currency.plural}")
                }
                "decimals" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.Currency.decimals)
                        return@doAsync
                    }
                    val (_, currencySearch) = args
                    val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@doAsync
                    }

                    currency.decimalSupported = !currency.decimalSupported
                    pluginInstance.dataHandler.saveCurrency(currency)
                    sender.sendMessage(
                        "${L.prefix}§7Toggled Decimal Support for §f${currency.plural} §7: ${yesNo(currency.decimalSupported)}"
                    )
                }
                "delete" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.Currency.delete)
                        return@doAsync
                    }
                    val (_, currencySearch) = args
                    val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@doAsync
                    }

                    pluginInstance.accountStore.accounts.filter { acc -> currency in acc.balances }
                        .forEach { it.balances.remove(currency) }
                    pluginInstance.dataHandler.deleteCurrency(currency)
                    pluginInstance.currencyStore.currencies.remove(currency)
                    if (pluginInstance.currencyStore.currencies.size == 1) {
                        val newDefault = pluginInstance.currencyStore.currencies.first()
                        newDefault.defaultCurrency = true
                        pluginInstance.dataHandler.saveCurrency(newDefault)
                    }
                    sender.sendMessage("${L.prefix}§7Deleted currency §a${currency.plural}§7 successfully")
                }
                "setrate" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.Currency.rate)
                        return@doAsync
                    }
                    val (_, currency, newStart) = args
                    setNewRate(sender, currency, newStart)
                }
                else -> {
                    sender.sendMessage(L.unknownSubCommand)
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
        // Subcommands
        if (args.size == 1)
            return subCommands.filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        // Currency autocomplete
        if (args.size == 2) {
            if (needsCurrency.any { it.equals(args[0], true) }) {
                val currencyKeys = pluginInstance.currencyStore.currencies.map { it.singular }.toMutableList()
                currencyKeys.addAll(pluginInstance.currencyStore.currencies.map { it.plural })

                return currencyKeys.filter {
                    it.startsWith(args[1], true)
                }.toMutableList()
            }
        }

        if (args.size == 3) {
            if (args[0].equals("create", true))
                return mutableListOf("${args[1]}s")
            if (args[0].equals("color", true))
                return possibleColors.filter {
                    it.startsWith(args[2], true)
                }.toMutableList()
            if (args[0].equals("symbol", true))
                return mutableListOf("remove")
        }

        if (args.size == 4 && args[0].equals("create", true))
            return PluginConfiguration.StorageSettings.currencyTypes.toMutableList()

        return mutableListOf()
    }

    private fun createCurrency(sender: CommandSender, single: String, plural: String, type: String) {
        if (pluginInstance.currencyStore.currencyExists(single) || pluginInstance.currencyStore.currencyExists(plural)) {
            sender.sendMessage("${L.prefix}§cCurrency already exists.")
            return
        }
        pluginInstance.currencyStore.createNewCurrency(single, plural, type)
        pluginInstance.logger.info("Created currency $single ($plural) with type $type")
        sender.sendMessage("${L.prefix}§7Created currency: §a$single")
    }

    private fun listCurrency(sender: CommandSender, search: String) {
        val currency = pluginInstance.currencyStore.getCurrency(search)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }
        val sendList = listOf(
            "§7ID: §c${currency.uuid}",
            "§7Type: §c${currency.type}",
            "§7Singular: §a${currency.singular}§7, Plural: §a${currency.plural}",
            "§7Start Balance: ${currency.color}${currency.format(currency.defaultBalance)}§7.",
            "§7Decimals: ${yesNo(currency.decimalSupported)}",
            "§7Default: ${yesNo(currency.defaultCurrency)}",
            "§7Color: ${currency.color}${currency.color.name.toLowerCase()}",
            "§7Rate: ${currency.exchangeRate}"
        )
        sendList.forEach { sender.sendMessage("${L.prefix}$it") }
    }

    private fun setNewStartBal(sender: CommandSender, currencySearch: String, newStartString: String) {
        val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }

        val newAmount = try {
            val temp = if (currency.decimalSupported) {
                newStartString.toBigDecimal()
            } else {
                newStartString.toBigInteger().toBigDecimal()
            }
            if (temp < BigDecimal.ZERO) throw NumberFormatException()
            temp
        } catch (ex: NumberFormatException) {
            sender.sendMessage(L.invalidAmount)
            BigDecimal(-111.111)
        }

        if (newAmount == BigDecimal(-111.111)) return

        currency.defaultBalance = newAmount
        pluginInstance.dataHandler.saveCurrency(currency)
        sender.sendMessage("${L.prefix}§7Starting balance for §f${currency.plural} §7was set to: §a${NumberUtilities.format(currency.defaultBalance)}")
    }

    private fun setNewRate(sender: CommandSender, currencySearch: String, newRateString: String) {
        val currency = pluginInstance.currencyStore.getCurrency(currencySearch)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }
        val newAmount = try {
            val temp = newRateString.toBigDecimal()
            if (temp < BigDecimal.ZERO) throw NumberFormatException()
            temp
        } catch (ex: NumberFormatException) {
            sender.sendMessage(L.invalidAmount)
            BigDecimal(-111.111)
        }

        if (newAmount == BigDecimal(-111.111)) return

        currency.exchangeRate = newAmount
        pluginInstance.dataHandler.saveCurrency(currency)
        sender.sendMessage(
            L.Currency.exchangeRateSet
                .replace("{currencycolor}", currency.color.toString())
                .replace("{currency}", currency.plural)
                .replace("{amount}", newAmount.toString())
        )
    }

    private fun yesNo(value: Boolean) = if (value) {
        "§aYes"
    } else {
        "§cNo"
    }
}
