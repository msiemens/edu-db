package de.msiemens.db.cursor

class TableCursor(
    private var current: Int,
    private val length: Int,
    private val emptyRows: Set<Int>
) : Cursor<Int> {
    override fun next(): Int {
        if (end()) error("Table cursor has reached the end")

        val c = current

        do {
            current += 1
        } while (current in emptyRows)

        return c
    }

    override fun end(): Boolean = current == length + emptyRows.size
}
