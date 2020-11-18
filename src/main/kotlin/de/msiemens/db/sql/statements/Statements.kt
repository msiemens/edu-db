package de.msiemens.db.sql.statements

import de.msiemens.db.query.Condition
import de.msiemens.db.table.Row
import de.msiemens.db.table.Value

sealed class Statement

data class CreateTableStatement(
    val table: String,
    val columns: MutableList<ColumnDefinition>
) : Statement()

data class DropTableStatement(
    val table: String,
) : Statement()

data class InsertStatement(
    val table: String,
    val row: Row
) : Statement()

data class SelectStatement(
    val table: String,
    val columns: List<String>?,
    val condition: Condition? = null,
    val order: Pair<String, OrderDirection>? = null,
    val limit: Int? = null,
    val offset: Int? = null,
) : Statement()

data class UpdateStatement(
    val table: String,
    val values: List<Pair<String, Value>>,
    val condition: Condition? = null,
) : Statement()

data class DeleteStatement(
    val table: String,
    val condition: Condition? = null,
) : Statement()

data class CreateIndexStatement(
    val name: String,
    val table: String,
    val column: String,
) : Statement()

object ShowTablesStatement : Statement()

data class ShowIndexStatement(val table: String) : Statement()
