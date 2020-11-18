package de.msiemens.db.table

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class TableTest {
    @Test
    fun cursor() {
        val table = Table(Schema(listOf("id" to ColumnType.INT)))
        table.insert(Row(mutableListOf(IntValue(1))))
        table.insert(Row(mutableListOf(IntValue(2))))
        table.insert(Row(mutableListOf(IntValue(3))))

        val cursor = table.cursor()

        assertFalse(cursor.end())
        assertEquals(0, cursor.next())

        assertFalse(cursor.end())
        assertEquals(1, cursor.next())

        assertFalse(cursor.end())
        assertEquals(2, cursor.next())

        assertTrue(cursor.end())
    }

    @Test
    fun cursorWithDeleted() {
        val table = Table(Schema(listOf("id" to ColumnType.INT)))
        table.insert(Row(mutableListOf(IntValue(1))))
        table.insert(Row(mutableListOf(IntValue(2))))
        table.insert(Row(mutableListOf(IntValue(3))))

        table.delete(0)

        val cursor = table.cursor()
        assertFalse(cursor.end())
        assertEquals(1, cursor.next())

        assertFalse(cursor.end())
        assertEquals(2, cursor.next())

        assertTrue(cursor.end())
    }

    @Test
    fun cursorWithAllDeleted() {
        val table = Table(Schema(listOf("id" to ColumnType.INT)))
        table.insert(Row(mutableListOf(IntValue(1))))
        table.insert(Row(mutableListOf(IntValue(2))))
        table.insert(Row(mutableListOf(IntValue(3))))

        table.delete(0)
        table.delete(1)
        table.delete(2)

        val cursor = table.cursor()
        assertTrue(cursor.end())
    }
}