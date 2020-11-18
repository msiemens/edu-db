package de.msiemens.db

import de.msiemens.db.engine.Executor
import de.msiemens.db.sql.Parser
import de.msiemens.db.sql.statements.*
import de.msiemens.db.table.Row
import de.msiemens.db.table.Schema
import de.msiemens.db.table.StringValue
import de.msiemens.db.table.Table

class DB {
    private var tables: MutableMap<String, Table> = mutableMapOf()
    private var executor = Executor()

    fun script(sql: String): List<List<Row>> {
        return Parser(sql).parseScript().map { exec(it) }
    }

    fun exec(sql: String): List<Row> {
        return exec(Parser(sql).parseStatement())
    }

    private fun exec(stmt: Statement): List<Row> {
        when (stmt) {
            is CreateTableStatement -> {
                val table = stmt.table

                check(table !in tables) { "Index $table already exists" }

                tables[table] = Table(Schema.of(stmt.columns))
            }
            is DropTableStatement -> {
                check(stmt.table in tables) { "No table named ${stmt.table}" }

                tables.remove(stmt.table)
            }
            is CreateIndexStatement -> table(stmt.table).createIndex(stmt.name, stmt.column)
            is InsertStatement -> executor.insert(table(stmt.table), stmt.row)
            is SelectStatement -> return executor.select(table(stmt.table), stmt)
            is UpdateStatement -> executor.update(table(stmt.table), stmt)
            is DeleteStatement -> executor.delete(table(stmt.table), stmt.condition)
            is ShowTablesStatement -> return showTables()
            is ShowIndexStatement -> return showIndex(stmt.table)
        }

        return listOf()
    }

    private fun table(tableName: String) = tables[tableName] ?: error("No table named $tableName")

    private fun showTables(): List<Row> = tables.keys.map { Row(mutableListOf(StringValue(it))) }

    private fun showIndex(table: String): List<Row> =
        table(table).indexes.keys.map { Row(mutableListOf(StringValue(it))) }
}
