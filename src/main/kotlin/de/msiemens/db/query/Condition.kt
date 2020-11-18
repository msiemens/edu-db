package de.msiemens.db.query

import de.msiemens.db.table.*

data class Condition(
    val column: String,
    val operator: Operator,
    val value: Value,
    var orClauses: List<Condition> = emptyList(),
    var andClauses: List<Condition> = emptyList(),
) {
    fun eval(row: Row, schema: Schema): Boolean {
        val idx = schema.index(column) ?: error("Unknown column $column")

        return eval(row, idx)
    }

    private fun eval(row: Row, idx: Int): Boolean {
        val value = when (operator) {
            Operator.EQ -> row.values[idx] == value
            Operator.NE -> row.values[idx] != value
            Operator.GT -> (row.values[idx] as IntValue) > (value as IntValue)
            Operator.GE -> (row.values[idx] as IntValue) >= (value as IntValue)
            Operator.LT -> (row.values[idx] as IntValue) < (value as IntValue)
            Operator.LE -> (row.values[idx] as IntValue) <= (value as IntValue)
            Operator.LIKE -> like((value as StringValue).value, (row.values[idx] as StringValue).value)
        }

        return (value && andClauses.all { it.eval(row, idx) }) || andClauses.any { it.eval(row, idx) }
    }

    private fun like(pattern: String, value: String): Boolean {
        val regex = pattern
            .replace(Regex("[\\W]"), "\\\\$0")
            .replace("%", ".*")
            .replace("_", ".")

        return Regex(regex).containsMatchIn(value)
    }
}
