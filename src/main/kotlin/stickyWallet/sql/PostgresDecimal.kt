package stickyWallet.sql

import org.jetbrains.exposed.sql.ColumnType
import java.math.BigDecimal
import java.math.MathContext

class PostgresDecimal : ColumnType() {
    override fun sqlType(): String  = "DECIMAL"
    override fun valueFromDB(value: Any): BigDecimal = when (value) {
        is BigDecimal -> value
        is Double -> value.toBigDecimal()
        is Float -> value.toBigDecimal()
        is Long -> value.toBigDecimal()
        is Int -> value.toBigDecimal()
        else -> error("Unexpected value of type Double: $value of ${value::class.qualifiedName}")
    }.round(MathContext.DECIMAL128)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as PostgresDecimal

        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}