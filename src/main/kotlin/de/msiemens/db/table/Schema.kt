package de.msiemens.db.table

import de.msiemens.db.sql.statements.ColumnDefinition

data class Schema(val columns: List<Pair<String, ColumnType>>) {
    fun names(): List<String> = columns.map { it.first }.toList()

    fun type(column: String): ColumnType = columns.first { it.first == column }.second

    fun index(column: String): Int? {
        val col = columns
            .withIndex()
            .firstOrNull { it.value.first == column }
            ?: return null

        return col.index
    }

    companion object {
        fun of(columns: List<ColumnDefinition>): Schema = Schema(columns.map { it.name to it.type }.toList())
    }
}
