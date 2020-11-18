package de.msiemens.db.cursor

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class TableCursorTest {
    @Test
    operator fun next() {
        val cursor = TableCursor(0, 4, setOf(3, 4))
        assertEquals(false, cursor.end())
        assertEquals(0, cursor.next())

        assertEquals(false, cursor.end())
        assertEquals(1, cursor.next())

        assertEquals(false, cursor.end())
        assertEquals(2, cursor.next())

        assertEquals(false, cursor.end())
        assertEquals(5, cursor.next())

        assertEquals(true, cursor.end())
    }

    @Test
    fun empty() {
        val cursor = TableCursor(0, 0, emptySet())
        assertEquals(true, cursor.end())
    }
}
