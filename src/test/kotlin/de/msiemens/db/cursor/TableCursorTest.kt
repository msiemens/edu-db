package de.msiemens.db.cursor

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class TableCursorTest {
    @Test
    operator fun next() {
        val cursor = TableCursor(0, 6, setOf(3, 4))
        assertEquals(0, cursor.next())
        assertEquals(1, cursor.next())
        assertEquals(2, cursor.next())
        assertEquals(5, cursor.next())
    }
}
