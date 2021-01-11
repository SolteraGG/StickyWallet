package stickyWallet.sql

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.stringParam

class ILikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "~*")

infix fun <T : String?> Expression<T>.ilike(pattern: String) = ILikeOp(this, stringParam(pattern))
