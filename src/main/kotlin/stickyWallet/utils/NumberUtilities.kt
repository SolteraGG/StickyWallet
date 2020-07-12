package stickyWallet.utils

import java.math.BigDecimal
import java.math.MathContext
import java.text.CompactNumberFormat
import java.text.DecimalFormat
import java.text.NumberFormat

object NumberUtilities {
    private val decimalFormat = DecimalFormat()
    private val compactFormat: NumberFormat

    private val patterns = listOf(
        "0",
        "00",
        "000",
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

        compactFormat.minimumFractionDigits = 3
    }

    fun format(money: BigDecimal): String {
        val roundOff = money.times(BigDecimal(100.0)).round(MathContext.DECIMAL128).divide(BigDecimal(100.0))

        return decimalFormat.format(roundOff)
    }

    fun compactFormat(money: BigDecimal): String = compactFormat.format(money)
}