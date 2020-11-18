package de.msiemens.db.sql.statements

import de.msiemens.db.table.ColumnType

data class ColumnDefinition(val name: String, val type: ColumnType)
