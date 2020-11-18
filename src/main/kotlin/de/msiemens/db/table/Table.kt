package de.msiemens.db.table

import de.msiemens.db.cursor.Cursor
import de.msiemens.db.cursor.TableCursor
import de.msiemens.db.index.Index
import de.msiemens.db.query.Condition

class Table(val schema: Schema) {
    private var rows: MutableList<Row?> = mutableListOf()
    internal var indexes: MutableMap<String, Index<in Value>> = mutableMapOf()
    private var indexesForColumn: MutableMap<String, String> = mutableMapOf()

    fun cursor(): Cursor<Int> {
        var start = rows.indexOfFirst { it != null }
        if (start < 0) {
            start = rows.size
        }

        val count = rows.count { it != null }

        val emptyRows = rows.withIndex()
            .filter { it.value == null }
            .map { it.index }
            .toSet()

        return TableCursor(start, count, emptyRows)
    }

    fun get(index: Int): Row? = rows.getOrNull(index)

    fun insert(row: Row) {
        // Perform insertion
        rows.add(row)

        // Update indexes
        val columnNames = schema.names()
        row.values
            .withIndex()
            .map { columnNames[it.index] to it.value }
            .map { indexesForColumn[it.first] to it.second }
            .map { indexes[it.first] to it.second }
            .forEach {
                it.first?.add(it.second, rows.lastIndex)
            }
    }

    fun update(idx: Int, values: List<Pair<Int, Value>>) {
        val columnNames = schema.names()
        val row = rows[idx] ?: error("Row with offset $idx has been deleted")

        for ((col, new) in values) {
            // Update row value
            val old = row.values[col]
            row.values[col] = new

            // Update indexes
            val indexName = indexesForColumn[columnNames[col]] ?: continue
            val index = indexes[indexName] ?: continue

            index.remove(old, idx)
            index.add(new, idx)
        }
    }

    fun delete(idx: Int) {
        val row = rows[idx] ?: error("Row with offset $idx has been deleted")

        val columnNames = schema.names()
        row.values
            .withIndex()
            .map { columnNames[it.index] to it.value }
            .map { indexesForColumn[it.first] to it.second }
            .map { indexes[it.first] to it.second }
            .forEach {
                it.first?.remove(it.second, idx)
            }

        rows[idx] = null
    }

    fun index(column: String, condition: Condition): List<Int>? {
        return indexes[indexesForColumn[column]]?.cursor(condition)
    }

    fun createIndex(name: String, column: String) {
        check(name !in indexes) { "Index $name already exists" }

        val index = when (schema.type(column)) {
            ColumnType.STRING -> createIndex<StringValue>(column)
            ColumnType.INT -> createIndex<IntValue>(column)
            null -> error("No such column $column")
        }

        indexes[name] = index as Index<Value>
        indexesForColumn[column] = name
    }

    private inline fun <reified T : Value> createIndex(column: String): Index<T> {
        val idx = schema.index(column) ?: error("Unknown column $column")

        return Index<T>().apply {
            fill(
                rows
                    .withIndex()
                    .map { it.index to it.value?.values?.get(idx) as T? }
                    .filter { it.second != null }
                    .map { it.first to it.second!! }
            )
        }
    }
}
