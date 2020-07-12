package stickyWallet.utils

import java.text.CompactNumberFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.round

object NumberUtilities {
    private val decimalFormat = DecimalFormat()
    private val compactFormat: NumberFormat

    private val patterns = listOf<String>(
        "",
        "",
        "",
        "0k",
        "00k",
        "000k",
        "0M",
        "00M",
        "000M",
        "0B",
        "00B",
        "000B",
        "0T",
        "00T",
        "000T",
        "0Qd",
        "00Qd",
        "000Qd",
        "0Qt",
        "00Qt",
        "000Qt",
        "0Sx",
        "00Sx",
        "000Sx",
        "0Sp",
        "00Sp",
        "000Sp",
        "0O",
        "00O",
        "000O",
        "0N",
        "00N",
        "000N",
        "0D",
        "00D",
        "000D"
    )

    init {
        val symbols = decimalFormat.decimalFormatSymbols
        symbols.groupingSeparator = ','
        symbols.decimalSeparator = '.'

        decimalFormat.decimalFormatSymbols = symbols
        decimalFormat.isGroupingUsed = true
        decimalFormat.groupingSize = 3

        compactFormat = CompactNumberFormat(
            decimalFormat.toPattern(),
            decimalFormat.decimalFormatSymbols,
            patterns.toTypedArray()
        )
    }

    fun format(money: Double): String {
        val roundOff = round(money * 100.0) / 100.0

        return decimalFormat.format(roundOff)
    }

    fun compactFormat(money: Double): String = compactFormat.format(money)
}