package stickyWallet.configs

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import stickyWallet.utils.StringUtilities.colorize

object L {
    val prefix = colorize("&5&ldddMC &8| &aWallet&r&8 &8&l>> &r")

    val noPermissions = colorize("$prefix&7You don't have permission to do this.")

    val invalidPage = colorize("$prefix&7That's not a valid page number.")

    val unknownCurrency = colorize("$prefix&7Unknown Currency.")

    val unknownSubCommand = colorize("$prefix&7Unknown sub-command.")

    val invalidAmount = colorize("$prefix&7That's not a valid amount.")

    val playerDoesNotExist = colorize("$prefix&7The specified player does not exist or could not be found.")

    val targetInsufficientFunds = colorize("$prefix&e{target} &7doesn't have enough {currencycolor}{currency}&7!")

    val noDefaultCurrency = colorize("$prefix&7No default currency set.")

    val noConsole = colorize("$prefix&7The Console cannot do this.")

    val accountMissing = colorize("$prefix&7Your account is missing. Please relog into the server.")

    object BalTop {
        val header = colorize("$prefix&f-- {currencycolor} Top Balances for {currencyplural} &7(Page {page})&f --")
        val empty = colorize("$prefix&7No accounts to display.")
        val entry = colorize("$prefix&a&l-> {number}. {currencycolor}{player} &7- {currencycolor}{balance}")
        val next = colorize("$prefix{currencycolor}/baltop {currencyplural} {page} &7for more.")
    }

    object Currency {
        fun sendUsage(sender: CommandSender) {
            sender.sendMessage(
                colorize(
                    """
                    $prefix&e&lCurrency Help
                    &2&l>> &a/currency create <singular> <plural> <type> &8- &7Create a currency.
                    &2&l>> &a/currency delete <plural> &8- &7Delete a currency.
                    &2&l>> &a/currency view <plural> &8- &7View information about a currency.
                    &2&l>> &a/currency list &8- &7List of currencies.
                    &2&l>> &a/currency symbol <plural> <char|remove> &8- &7Select a symbol for a currency or remove it.
                    &2&l>> &a/currency color <plural> <color> &8- &7Select a color for a currency.
                    &2&l>> &a/currency colorlist &8- &7List of Colors.
                    &2&l>> &a/currency decimals <plural> &8- &7Enable decimals for a currency.
                    &2&l>> &a/currency payable <plural> &8- &7Set a currency payable or not.
                    &2&l>> &a/currency default <plural> &8- &7Set a currency as default.
                    &2&l>> &a/currency startbal <plural> <amount> &8- &7Set the starting balance for a currency.
                    &2&l>> &a/currency setrate <plural> <amount> &8- &7Sets the currency's exchange rate.
                    """.trimIndent()
                )
            )
        }

        val create = colorize("$prefix&2&l>> &a/currency create <singular> <plural> <type> &8- &7Create a currency.")

        val view = colorize("$prefix&2&l>> &a/currency view <plural> &8- &7View information about a currency.")

        val startBal =
            colorize("$prefix&2&l>> &a/currency startbal <plural> <amount> &8- &7Set the starting balance for a currency.")

        val color = colorize("$prefix&2&l>> &a/currency color <plural> <color> &8- &7Select a color for a currency.")

        val symbol =
            colorize("$prefix&2&l>> &a/currency symbol <plural> <char|remove> &8- &7Select a symbol for a currency or remove it.")

        val default = colorize("$prefix&2&l>> &a/currency default <plural> &8- &7Set a currency as default.")

        val decimals = colorize("$prefix&2&l>> &a/currency decimals <plural> &8- &7Toggles decimals for a currency.")

        val delete = colorize("$prefix&2&l>> &a/currency delete <plural> &8- &7Delete a currency.")

        val rate =
            colorize("$prefix&2&l>> &a/currency setrate <plural> <amount> &8- &7Sets the currency's exchange rate.")

        val exchangeRateSet =
            colorize("$prefix&7Set the exchange rate for {currencycolor}{currency} &7to &a{amount}&7.")
    }

    object Economy {
        fun sendUsage(sender: CommandSender) {
            sender.sendMessage(
                colorize(
                    """
                    $prefix&e&lEconomy Help
                    &2&l>> &a/eco give <user> <amount> [currency] &8- &7Give a player an amount of a currency.
                    &2&l>> &a/eco take <user> <amount> [currency] &8- &7Take an amount of a currency from a player.
                    &2&l>> &a/eco set <user> <amount> [currency] &8- &7Set a players amount of a currency.
                    """.trimIndent()
                )
            )
        }

        val give =
            colorize("$prefix&2&l>> &a/eco give <user> <amount> [currency] &8- &7Give a player an amount of a currency.")

        val take =
            colorize("$prefix&2&l>> &a/eco take <user> <amount> [currency] &8- &7Take an amount of a currency from a player.")

        val set =
            colorize("$prefix&2&l>> &a/eco set <user> <amount> [currency] &8- &7Set a players amount of a currency.")

        val addResult = colorize("$prefix&7You gave &a{player}&7: {currencycolor}{amount}.")

        val takeResult = colorize("$prefix&7You took {currencycolor}{amount} &7from &a{player}&7.")

        val setResult = colorize("$prefix&7You set &a{player}&7's balance to {currencycolor}{amount}&7.")
    }

    object Balance {
        val none = colorize("$prefix&7No balances to show for &c{player}&7.")

        val currennt = colorize("$prefix&a{player}&7's balance is: {currencycolor}{balance}")

        val multipleHeader = colorize("$prefix&a{player}&7's balances:")

        val multipleEntry = colorize("$prefix&a&l>> {currencycolor}{format}")
    }

    object Pay {
        val usage =
            colorize("$prefix&2&l>> &a/pay <user> <amount> [currency] &8- &7Pay the specified user the specified amount.")

        val yourself = colorize("$prefix&7You can't pay yourself.")

        val noPerms = colorize("$prefix&7You don't have permission to pay with {currencycolor}{currency}&7.")

        val insufficientFunds = colorize("$prefix&7You don't have enough {currencycolor}{currency}&7!")

        val paid = colorize("$prefix&7You were paid {currencycolor}{amount} &7from &a{player}&7.")

        val payer = colorize("$prefix&7You paid {currencycolor}{amount} &7to &a{player}&7.")
    }

    object Check {
        val success = colorize("$prefix&7Check successfully written.")

        val psstError = colorize("$prefix&7Psst..specify an online player!")

        val psstOk = colorize("$prefix&7They got the check! It'll be our little secret")

        val invalid = colorize("$prefix&7You don't have a valid check in any of your hands...")

        val redeemed = colorize("$prefix&7Check has been cashed in.")

        val noPerms = colorize("$prefix&7You don't have permission to write a check for {currencycolor}{currency}&7.")

        fun sendUsage(sender: CommandSender) {
            sender.sendMessage(
                colorize(
                    """
                    $prefix&e&lCheck Help
                    &2&l>> &a/check write <amount> [currency] ${
                    if (sender !is Player) {
                        "<player name>"
                    } else {
                        ""
                    }
                    } &8- &7Write a check with a specified amount and currency.
                    &2&l>> &a/check redeem &8- &7Redeem the check.
                    """.trimIndent()
                )
            )
        }
    }
}
