package stickyWallet.apis

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import stickyWallet.accounts.Account
import stickyWallet.currencies.Currency
import stickyWallet.interfaces.UsePlugin
import stickyWallet.sql.tables.BalancesTable
import java.math.BigDecimal
import java.util.UUID

class PlaceholderAPI : PlaceholderExpansion(), UsePlugin {

    override fun getVersion() = pluginInstance.description.version

    override fun getAuthor() = pluginInstance.description.authors.joinToString(", ")

    override fun getIdentifier() = "stickywallet"

    override fun persist() = true

    override fun canRegister() = true

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (player == null) return ""

        val split = identifier.toLowerCase().split("-")

        // All our placeholders have at LEAST two entries
        if (split.size < 2) return null

        val (base) = split

        return when (base) {
            "balance" -> parseBalanceTag(player, split.subList(1, split.size))
            "baltop" -> parseBalTopTag(split.subList(1, split.size))
            else -> null
        }
    }

    /**
     * Handles:
     * - balance-<currency>
     * - balance-compact-<currency>
     * - balance-raw-<currency>
     * - balance-rawcompact-<currency>
     */
    private fun parseBalanceTag(player: Player, tagPieces: List<String>): String? {
        val (currency, compact, raw) = if (tagPieces.size == 1) {
            Triple(tagPieces[0], second = false, third = false)
        } else {
            var toBeCompact = false
            var toBeRaw = false

            if (tagPieces[0].endsWith("compact", true)) {
                toBeCompact = true
            }

            if (tagPieces[0].startsWith("raw", true)) {
                toBeRaw = true
            }

            Triple(tagPieces[1], toBeCompact, toBeRaw)
        }

        val (account, cachedCurrency) = getAccountAndCurrency(player, currency) ?: return "Unknown Currency"
        val balance = account.getBalanceForCurrency(cachedCurrency)

        return cachedCurrency.format(balance, compact = compact, raw = raw)
    }

    /**
     * Handles:
     * - baltop-position-<number>-<currency>
     * - baltop-compact-position-<number>-<currency>
     * - baltop-player-<number>-<currency>
     */
    private fun parseBalTopTag(tagPieces: List<String>): String? {
        // We must have at least 3 extra pieces
        if (tagPieces.size < 3) return null

        val (type, position, currencyKey) = if (tagPieces.size == 3) {
            Triple(tagPieces[0].toLowerCase(), tagPieces[1].toLong(), tagPieces[2])
        } else {
            Triple(tagPieces[1] + "_compact", tagPieces[2].toLong(), tagPieces[3])
        }

        val currency = pluginInstance.currencyStore.getCurrency(currencyKey) ?: return "Unknown Currency"

        val (username, formattedAmount) = fetchUserAtPosition(currency, position, compact = type.endsWith("_compact"))

        return when (type.toLowerCase()) {
            "position" -> formattedAmount
            "position_compact" -> formattedAmount
            "player" -> username
            else -> null
        }
    }

    private fun getAccountAndCurrency(player: Player, currencyKey: String): Pair<Account, Currency>? {
        val account = pluginInstance.accountStore.getAccount(player.uniqueId)
        val cachedCurrency = pluginInstance.currencyStore.getCurrency(currencyKey)

        if (account == null || cachedCurrency == null) return null

        return Pair(account, cachedCurrency)
    }

    private fun fetchUserAtPosition(currency: Currency, position: Long, compact: Boolean = false): Pair<String, String> {
        val result = transaction(pluginInstance.dataHandler.dbConnection) {
            BalancesTable
                .select { (BalancesTable.currencyID eq currency.uuid.toString()) }
                .orderBy(BalancesTable.balance to SortOrder.DESC)
                .limit(1, offset = position - 1)
                .firstOrNull()
        } ?: return Pair("???", currency.format(BigDecimal.ZERO))

        val account = pluginInstance.accountStore.getAccount(UUID.fromString(result[BalancesTable.accountID]))
            ?: return Pair("???", currency.format(BigDecimal.ZERO))

        return Pair(account.playerName!!, currency.format(account.getBalanceForCurrency(currency), compact))
    }
}
