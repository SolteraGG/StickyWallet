package stickyWallet.utils

import org.apache.commons.lang.math.NumberUtils
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.pow


object StickyNumberUtils {

    class CompactNumberFormatter: Format() {
        private val METRIC_PREFIXES = arrayOf("", "k", "M", "G", "T")

        /**
         * The maximum number of characters in the output, excluding the negative sign
         */
        private val MAX_LENGTH = 4

        private val TRAILING_DECIMAL_POINT: Pattern = Pattern.compile("[0-9]+\\.[kMGT]")

        private val METRIC_PREFIXED_NUMBER: Pattern = Pattern.compile("\\-?[0-9]+(\\.[0-9])?[kMGT]")

        override fun format(obj: Any, output: StringBuffer, pos: FieldPosition?): StringBuffer? {
            var number = java.lang.Double.valueOf(obj.toString())

            // if the number is negative, convert it to a positive number and add the minus sign to the output at the end
            val isNegative = number < 0
            number = abs(number)
            var result = DecimalFormat("##0E0").format(number)
            val index = Character.getNumericValue(result[result.length - 1]) / 3
            result = result.replace("E[0-9]".toRegex(), METRIC_PREFIXES[index])
            while (result.length > MAX_LENGTH || TRAILING_DECIMAL_POINT.matcher(result).matches()) {
                val length = result.length
                result = result.substring(0, length - 2) + result.substring(length - 1)
            }
            return output.append(if (isNegative) "-$result" else result)
        }

        /**
         * Convert a String produced by <tt>format()</tt> back to a number. This will generally not restore
         * the original number because <tt>format()</tt> is a lossy operation, e.g.
         *
         * <pre>
         * `def formatter = new RoundedMetricPrefixFormat()
         * Long number = 5821L
         * String formattedNumber = formatter.format(number)
         * assert formattedNumber == '5.8k'
         *
         * Long parsedNumber = formatter.parseObject(formattedNumber)
         * assert parsedNumber == 5800
         * assert parsedNumber != number
        ` *
        </pre> *
         *
         * @param source a number that may have a metric prefix
         * @param pos if parsing succeeds, this should be updated to the index after the last parsed character
         * @return a Number if the the string is a number without a metric prefix, or a Long if it has a metric prefix
         */
        override fun parseObject(source: String, pos: ParsePosition): Any? {
            if (NumberUtils.isNumber(source)) {

                // if the value is a number (without a prefix) don't return it as a Long or we'll lose any decimals
                pos.index = source.length
                return toNumber(source)
            } else if (METRIC_PREFIXED_NUMBER.matcher(source).matches()) {
                val isNegative = source[0] == '-'
                val length = source.length
                val number =
                    if (isNegative) source.substring(1, length - 1) else source.substring(0, length - 1)
                val metricPrefix = source[length - 1].toString()
                val absoluteNumber = toNumber(number)
                var index = 0
                while (index < METRIC_PREFIXES.size) {
                    if (METRIC_PREFIXES[index] == metricPrefix) {
                        break
                    }
                    index++
                }
                val exponent = 3 * index
                var factor = 10.0.pow(exponent.toDouble())
                factor *= (if (isNegative) -1 else 1).toDouble()
                pos.index = source.length
                val result: Float = absoluteNumber.toFloat() * factor.toLong()
                return result.toLong()
            }
            return null
        }

        private fun toNumber(number: String): Number {
            return NumberUtils.createNumber(number)
        }
    }

}