package stickyWallet.utils

import java.math.BigDecimal

val balances = listOf(
    128.0,
    128.5,
    1000.0,
    1000.5,
    10000.0,
    100000.0,
    1000000.0,
    10000000.0,
    100000000.0,
    1000000000.0,
    10000000000.0,
    100000000000.0,
    1000000000000.0,
    10000000000000.0,
    100000000000000.0,
    1000000000000000.0
)


//val compactFormats = mutableListOf(
////  1
//    "",
////   10
//    "",
////    100
//    ""
//)
//
//val formats = listOf("k", "M", "B", "T", "Qu", "Qt", "Sx", "Sp", "O", "N", "D")
//
//for (format in formats) {
//    compactFormats.add("0$format")
//    compactFormats.add("00$format")
//    compactFormats.add("000$format")
//}
//
//val decimalFormat = DecimalFormat()
//val symbols = decimalFormat.decimalFormatSymbols!!
//
//symbols.groupingSeparator = ','
//symbols.decimalSeparator = '.'
//
//decimalFormat.decimalFormatSymbols = symbols
//decimalFormat.isGroupingUsed = true
//decimalFormat.groupingSize = 3
//
//val compactFormatter = CompactNumberFormat()