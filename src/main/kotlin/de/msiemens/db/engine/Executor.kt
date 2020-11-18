package de.msiemens.db.engine

import de.msiemens.db.query.Condition
import de.msiemens.db.sql.statements.OrderDirection
import de.msiemens.db.sql.statements.SelectStatement
import de.msiemens.db.sql.statements.UpdateStatement
import de.msiemens.db.table.Row
import de.msiemens.db.table.Table

class Executor {
    fun select(
        table: Table,
        stmt: SelectStatement,
    ): List<Row> {
        val idx = select(table, stmt.condition)

        // Fetch the rows
        val rows = idx.map { table.get(it) ?: error("Row with offset $it is missing") }

        // Apply the columns projection
        var results = rows.map { project(table, stmt.columns, it) }

        // Apply sorting
        if (stmt.order != null) {
            results = sort(results, stmt.columns ?: table.schema.names(), stmt.order)
        }

        // Apply limit/offset
        if (stmt.limit != null) {
            check(stmt.limit >= 0) { "Limit must be non-negative" }

            results = if (stmt.offset != null) {
                check(stmt.offset >= 0) { "Offset must be non-negative" }

                results.subList(stmt.offset, stmt.offset + stmt.limit)
            } else {
                results.subList(0, stmt.limit)
            }
        }

        return results
    }

    fun update(
        table: Table,
        stmt: UpdateStatement,
    ) {
        // Validate inputs
        val valuesWithIndex = stmt.values.map {
            (table.schema.index(it.first) ?: error("No column named ${it.first}")) to it.second
        }
        valuesWithIndex.forEach {
            val expect = table.schema.columns[it.first].second
            val got = it.second.type

            check(expect == got) { "Type mismatch: expected $expect, got $got" }
        }

        // Update all matching rows
        for (row in select(table, stmt.condition)) {
            table.update(row, valuesWithIndex)
        }
    }

    fun insert(table: Table, row: Row) {
        // Verify inputs
        check(row.values.size == table.schema.columns.size) { "Column count does not match" }

        row.values
            .zip(table.schema.columns.map { it.second })
            .forEach {
                check(it.first.type == it.second) { "Column type mismatch: expected ${it.second} but got ${it.first.type}" }
            }

        // Insert the row
        table.insert(row)
    }

    fun delete(table: Table, condition: Condition?) {
        // Delete all matching rows
        for (row in select(table, condition)) {
            table.delete(row)
        }
    }

    private fun select(
        table: Table,
        condition: Condition?
    ): Set<Int> {
        // Select rows matching the query condition
        var rows = if (condition != null) {
            (table.index(condition.column, condition) ?: scan(table, condition)).toSet()
        } else {
            scan(table, condition).toSet()
        }

        // Apply and/our clauses to widen/narrow the result set
        if (condition != null) {
            rows = condition.andClauses
                .map { select(table, it).toSet() }
                .fold(rows, { acc, set -> acc intersect set })

            rows = condition.orClauses
                .map { select(table, it).toSet() }
                .fold(rows, { acc, set -> acc union set })
        }

        return rows
    }

    private fun scan(
        table: Table,
        condition: Condition?
    ): List<Int> {
        val results: MutableList<Int> = mutableListOf()
        val cursor = table.cursor()

        while (!cursor.end()) {
            val idx = cursor.next()
            val row = table.get(idx) ?: error("Cursor points to missing row id $idx")

            if (condition == null || condition.eval(row, table.schema)) {
                results.add(idx)
            }
        }

        return results
    }

    private fun project(
        table: Table,
        columns: List<String>?,
        row: Row
    ): Row {
        if (columns == null) {
            return row
        }

        val values = table.schema.names()
            .withIndex()
            .filter { it.value in columns }
            .map { row.values[it.index] }
            .toMutableList()

        return Row(values)
    }

    private fun sort(
        results: List<Row>,
        columns: List<String>,
        order: Pair<String, OrderDirection>
    ): List<Row> {
        val columnIndex = columns.indexOf(order.first)

        return when (order.second) {
            OrderDirection.ASC -> results.sortedBy { it.values[columnIndex] }
            OrderDirection.DESC -> results.sortedByDescending { it.values[columnIndex] }
        }
    }
}
