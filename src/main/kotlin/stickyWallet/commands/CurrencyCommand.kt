package stickyWallet.commands

import StickyPlugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import stickyWallet.currency.Currency
import stickyWallet.data.YamlStorage
import stickyWallet.files.L
import stickyWallet.utils.Permissions
import stickyWallet.utils.ServerUtils
import stickyWallet.utils.StringUtils
import java.io.File

class CurrencyCommand : TabExecutor {

    private val plugin = StickyPlugin.instance
    private val subCommands = listOf(
        "backend",
        "color",
        "colorlist",
        "convert",
        "create",
        "decimals",
        "default",
        "delete",
        "list",
        "payable",
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
        "payable",
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
        StickyPlugin.doAsync(Runnable {
            if (!sender.hasPermission(Permissions.COMMAND_CHECK)) {
                sender.sendMessage(L.noPermissions)
                return@Runnable
            }
            if (args.isEmpty()) {
                L.CurrencyUsage.sendUsage(sender)
                return@Runnable
            }

            val (stringCommand) = args

            when (stringCommand.toLowerCase()) {
                "create" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.CurrencyUsage.create)
                        return@Runnable
                    }
                    val (_, singular, plural) = args
                    createCurrency(sender, singular, plural)
                }
                "list" -> {
                    val currencies = plugin.currencyManager.currencies
                    val builder = StringBuilder().append(L.prefix).append("§7There ")
                        .append(StringUtils.pluralize(currencies.size, "is", "are"))
                        .append(" §f${currencies.size}§7 ")
                        .append(StringUtils.pluralize(currencies.size, "currency", "currencies"))
                    sender.sendMessage(builder.toString())
                    currencies.forEach {
                        sender.sendMessage("§a§l>> §e${it.singular}")
                    }
                }
                "view" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.view)
                        return@Runnable
                    }
                    val (_, currency) = args
                    listCurrency(sender, currency)
                }
                "startbal" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.CurrencyUsage.startBal)
                        return@Runnable
                    }
                    val (_, currency, newStart) = args
                    setNewStartBal(sender, currency, newStart)
                }
                "color" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.CurrencyUsage.color)
                        return@Runnable
                    }
                    val (_, currencySearch, newColor) = args
                    if (!possibleColors.any { it.equals(newColor, true) }) {
                        sender.sendMessage("${L.prefix}§cInvalid chat color. Use the autocomplete feature, pleb")
                        return@Runnable
                    }
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    val color = ChatColor.valueOf(newColor.toUpperCase())
                    currency.color = color
                    plugin.dataStore.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7The color for §f${currency.plural} §7was set to: $color${color.name}")
                }
                "colorlist" -> {
                    possibleColors.map { color ->
                        if (color == "reset") {
                            "${ChatColor.WHITE}§l${color.capitalize()} §7= $color"
                        } else {
                            "${ChatColor.valueOf(color.toUpperCase())}§l${color.split("_").joinToString(" ") {
                                it.capitalize()
                            }} §7= $color"
                        }
                    }
                        .forEach {
                            sender.sendMessage(it)
                        }
                }
                "symbol" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.CurrencyUsage.symbol)
                        return@Runnable
                    }
                    val (_, currencySearch, resetOrNewSymbol) = args
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    val newSymbol: String?

                    when {
                        resetOrNewSymbol.equals("remove", true) -> {
                            newSymbol = null
                            sender.sendMessage("${L.prefix}§7Currency symbol removed for §f${currency.plural}")
                        }
                        resetOrNewSymbol.length == 1 -> {
                            newSymbol = resetOrNewSymbol
                            sender.sendMessage("${L.prefix}§7Currency symbol for §f${currency.plural} §7was updated to: §a${resetOrNewSymbol}")
                        }
                        else -> {
                            sender.sendMessage("${L.prefix}§7Symbol must be 1 character or removed with \"${ChatColor.AQUA}remove§7\"")
                            return@Runnable
                        }
                    }

                    currency.symbol = newSymbol
                    plugin.dataStore.saveCurrency(currency)
                }
                "default" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.default)
                        return@Runnable
                    }
                    val (_, currencySearch) = args
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    val defaultCurrency = plugin.currencyManager.getDefaultCurrency()
                    if (defaultCurrency != null && defaultCurrency != currency) {
                        defaultCurrency.defaultCurrency = false
                        plugin.dataStore.saveCurrency(defaultCurrency)
                    }

                    currency.defaultCurrency = true
                    plugin.dataStore.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7Set default currency to §f${currency.plural}")
                }
                "payable" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.payable)
                        return@Runnable
                    }
                    val (_, currencySearch) = args
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    currency.payable = !currency.payable
                    plugin.dataStore.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7Toggled pay-ability for §f${currency.plural} §7: ${yesNo(currency.payable)}")
                }
                "decimals" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.decimals)
                        return@Runnable
                    }
                    val (_, currencySearch) = args
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    currency.decimalSupported = !currency.decimalSupported
                    plugin.dataStore.saveCurrency(currency)
                    sender.sendMessage("${L.prefix}§7Toggled Decimal Support for §f${currency.plural} §7: ${yesNo(currency.decimalSupported)}")
                }
                "delete" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.delete)
                        return@Runnable
                    }
                    val (_, currencySearch) = args
                    val currency = plugin.currencyManager.getCurrency(currencySearch)
                    if (currency == null) {
                        sender.sendMessage(L.unknownCurrency)
                        return@Runnable
                    }

                    plugin.accountManager.accounts.filter { currency in it.currencyBalances }
                        .forEach { it.currencyBalances.remove(currency) }
                    plugin.dataStore.deleteCurrency(currency)
                    plugin.currencyManager.currencies.remove(currency)
                    if (plugin.currencyManager.currencies.size == 1) {
                        val newDefault = plugin.currencyManager.currencies.first()
                        newDefault.defaultCurrency = true
                        plugin.dataStore.saveCurrency(newDefault)
                    }
                    sender.sendMessage("${L.prefix}§7Deleted currency §a${currency.plural}§7 successfully")
                }
                "setrate" -> {
                    if (args.size != 3) {
                        sender.sendMessage(L.CurrencyUsage.rate)
                        return@Runnable
                    }
                    val (_, currency, newStart) = args
                    setNewRate(sender, currency, newStart)
                }
                "convert" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.convert)
                        return@Runnable
                    }
                    val (_, method) = args
                    convertCurrencies(sender, method)
                }
                "backend" -> {
                    if (args.size != 2) {
                        sender.sendMessage(L.CurrencyUsage.backend)
                        return@Runnable
                    }
                    val (_, method) = args
                    switchBackend(sender, method)
                }
                else -> {
                    sender.sendMessage(L.unknownSubCommand)
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
            return subCommands.filter {
                it.startsWith(args[0], true)
            }.toMutableList()

        if (args.size == 2) {
            if (needsCurrency.any { it.equals(args[0], true) }) {
                val currencyKeys = plugin.currencyManager.currencies.map { it.singular }.toMutableList()
                currencyKeys.addAll(plugin.currencyManager.currencies.map { it.plural })

                return currencyKeys.filter {
                    it.startsWith(args[1], true)
                }.toMutableList()
            }
            if (
                args[0].equals("convert", true) ||
                args[0].equals("backend", true)
            ) return plugin.dataStoreManager
                .methods
                .map { it.name }
                .filter { it.startsWith(args[1], true) }
                .toMutableList()
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

        return mutableListOf()
    }

    private fun createCurrency(sender: CommandSender, single: String, plural: String) {
        if (plugin.currencyManager.currencyExists(single) || plugin.currencyManager.currencyExists(plural)) {
            sender.sendMessage("${L.prefix}§cCurrency already exists.")
            return
        }
        plugin.currencyManager.createNewCurrency(single, plural)
        ServerUtils.log("Created currency $single ($plural)")
        sender.sendMessage("${L.prefix}§7Created currency: §a${single}")
    }

    private fun listCurrency(sender: CommandSender, search: String) {
        val currency = plugin.currencyManager.getCurrency(search)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }
        val sendList = listOf(
            "§7ID: §c${currency.uuid}",
            "§7Singular: §a${currency.singular}§7, Plural: §a${currency.plural}",
            "§7Start Balance: ${currency.color}${currency.format(currency.defaultBalance)}§7.",
            "§7Decimals: ${yesNo(currency.decimalSupported)}",
            "§7Default: ${yesNo(currency.defaultCurrency)}",
            "§7Payable: ${yesNo(currency.payable)}",
            "§7Color: ${currency.color}${currency.color.name.toLowerCase()}",
            "§7Rate: ${currency.exchangeRate}"
        )
        sendList.forEach { sender.sendMessage("${L.prefix}$it") }
    }

    private fun setNewStartBal(sender: CommandSender, currencySearch: String, newStartString: String) {
        val currency = plugin.currencyManager.getCurrency(currencySearch)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }

        val newAmount = try {
            val temp = if (currency.decimalSupported) {
                newStartString.toDouble()
            } else {
                newStartString.toInt().toDouble()
            }
            if (temp < 0.0) throw NumberFormatException()
            temp
        } catch (ex: NumberFormatException) {
            sender.sendMessage(L.invalidAmount)
            -111.111
        }

        if (newAmount == -111.111) return

        currency.defaultBalance = newAmount
        plugin.dataStore.saveCurrency(currency)
        sender.sendMessage("${L.prefix}§7Starting balance for §f${currency.plural} §7was set to: §a${StringUtils.format(currency.defaultBalance)}")
    }

    private fun setNewRate(sender: CommandSender, currencySearch: String, newRateString: String) {
        val currency = plugin.currencyManager.getCurrency(currencySearch)
        if (currency == null) {
            sender.sendMessage(L.unknownCurrency)
            return
        }
        val newAmount = try {
            val temp = newRateString.toDouble()
            if (temp < 0.0) throw NumberFormatException()
            temp
        } catch (ex: NumberFormatException) {
            sender.sendMessage(L.invalidAmount)
            -111.111
        }

        if (newAmount == -111.111) return

        currency.exchangeRate = newAmount
        plugin.dataStore.saveCurrency(currency)
        sender.sendMessage(
            L.exchangeRateSet
                .replace("{currencycolor}", currency.color.toString())
                .replace("{currency}", currency.plural)
                .replace("{amount}", newAmount.toString())
        )
    }

    private fun convertCurrencies(sender: CommandSender, newMethod: String) {
        val currentDataStore = plugin.dataStore
        val newDataStore = plugin.dataStoreManager.getStore(newMethod)

        if (newDataStore == null) {
            sender.sendMessage("${L.prefix}§cData storage method not found.")
            return
        }

        if (currentDataStore.name.equals(newDataStore.name, true)) {
            sender.sendMessage("${L.prefix}You cannot convert to the same data store")
            return
        }

        // Update config
        plugin.config.set("storage", newDataStore.name)
        plugin.saveConfig()

        // Fetch all accounts from the old store
        sender.sendMessage("${L.prefix}§aLoading accounts from the old store..")
        plugin.accountManager.accounts.clear()

        val oldAccounts = currentDataStore.loadAllAccounts()
        sender.sendMessage("${L.prefix}§aLoaded accounts from the old store..")

        // Fetch all currencies from the old store
        sender.sendMessage("${L.prefix}§aLoading currencies from the old store..")
        val oldCurrencies = ArrayList(plugin.currencyManager.currencies)

        plugin.currencyManager.currencies.clear()
        sender.sendMessage("${L.prefix}§aLoaded currencies from the old store..")

        // Log debug
        if (plugin.debug) {
            oldAccounts.forEach {
                ServerUtils.log("Account ${it.displayName} has ${it.currencyBalances.size} balances. Map print: ${it.currencyBalances}")
            }
            oldCurrencies.forEach {
                ServerUtils.log("Currency ${it.singular} (${it.plural})")
            }
        }

        sender.sendMessage("${L.prefix}§aSwitching from §f${currentDataStore.name} §ato §f${newDataStore.name}§a.")
        if (newDataStore.name.equals("yaml", true)) {
            StickyPlugin.doSync(Runnable {
                val data = File("${plugin.dataFolder}${File.separator}data.yml")
                if (data.exists()) data.delete()
            })
        }

        currentDataStore.close()
        sender.sendMessage("${L.prefix}§aThe old data store is closed..")

        plugin.initializeDataStore(newDataStore.name, false)
        try {
            Thread.sleep(2000)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }

        sender.sendMessage("${L.prefix}§aInitialized the ${newDataStore.name} data store. Check your console for wrong username/password errors if you're using MySQL")

        val backupStore = YamlStorage(File("${plugin.dataFolder}${File.separator}data_backup.yml"))
        backupStore.initialize()

        oldCurrencies.forEach {
            val newCurrency = Currency(it.uuid, it.singular, it.plural)
            newCurrency.exchangeRate = it.exchangeRate
            newCurrency.defaultCurrency =  it.defaultCurrency
            newCurrency.symbol = it.symbol
            newCurrency.color = it.color
            newCurrency.decimalSupported = it.decimalSupported
            newCurrency.payable = it.payable
            newCurrency.defaultCurrency = it.defaultCurrency
            newDataStore.saveCurrency(newCurrency)
            backupStore.saveCurrency(newCurrency)
        }

        sender.sendMessage("${L.prefix}§aSaved old currencies to the new storage..")
        newDataStore.loadCurrencies()
        sender.sendMessage("${L.prefix}§aLoaded currencies as normal..")

        try {
            Thread.sleep(2000)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }

        oldAccounts.forEach {
            newDataStore.saveAccount(it)
            backupStore.saveAccount(it)
        }
        sender.sendMessage("${L.prefix}§aSaved old accounts to the new storage..")

        try {
            Thread.sleep(2000)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }

        Bukkit.getOnlinePlayers()
            .forEach { newDataStore.loadAccount(it.uniqueId) }

        sender.sendMessage("${L.prefix}§aLoaded all accounts for online players..")
        sender.sendMessage("${L.prefix}§aData storage conversion is done!")
    }

    private fun switchBackend(sender: CommandSender, newMethod: String) {
        val currentDataStore = plugin.dataStore
        val newDataStore = plugin.dataStoreManager.getStore(newMethod)

        if (newDataStore == null) {
            sender.sendMessage("${L.prefix}§cData storage method not found.")
            return
        }

        if (currentDataStore.name.equals(newDataStore.name, true)) {
            sender.sendMessage("${L.prefix}You cannot convert to the same data store")
            return
        }

        // Update config
        plugin.config.set("storage", newDataStore.name)
        plugin.saveConfig()

        sender.sendMessage("${L.prefix}§aSaving data and closing up..")
        currentDataStore.close()
        plugin.accountManager.accounts.clear()
        plugin.currencyManager.currencies.clear()
        sender.sendMessage("${L.prefix}§aSuccessfully saved. Launching the new backend..")

        sender.sendMessage("${L.prefix}§aSwitching from §f${currentDataStore.name} §ato §f${newDataStore.name}§a.")
        plugin.initializeDataStore(newDataStore.name, true)
        try {
            Thread.sleep(1000)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }

        Bukkit.getOnlinePlayers()
            .forEach { newDataStore.loadAccount(it.uniqueId) }

        sender.sendMessage("${L.prefix}§aLoaded all accounts for online players")
        sender.sendMessage("${L.prefix}§aThe new backend loaded successfully!")
    }

    private fun yesNo(value: Boolean) = if (value) {
        "§aYes"
    } else {
        "§cNo"
    }

}