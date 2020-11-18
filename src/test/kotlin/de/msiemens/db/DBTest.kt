package de.msiemens.db

import de.msiemens.db.table.IntValue
import de.msiemens.db.table.Row
import de.msiemens.db.table.StringValue
import de.msiemens.db.table.Value
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class DBTest {
    private val empty = emptyList<Row>()

    @Test
    fun exec() {
        val db = DB()

        assertEquals(empty, db.exec("create table names(id integer, value string)"))

        assertEquals(listOf(row(s("names"))), db.exec("show tables"))
        assertEquals(empty, db.exec("show index from names"))

        assertEquals(empty, db.exec("insert into names values(1, \"John\")"))
        assertEquals(empty, db.exec("insert into names values(2, \"Jane\")"))
        assertEquals(listOf(row(s("John"))), db.exec("select value from names where id = 1"))
        assertEquals(listOf(row(i(2))), db.exec("select id from names where value = \"Jane\""))
        assertEquals(listOf(row(i(2))), db.exec("select id from names where value like \"Ja%\""))
        assertEquals(
            listOf(
                row(i(2), s("Jane")),
                row(i(1), s("John"))
            ),
            db.exec("select * from names order by id desc")
        )
        assertEquals(listOf(row(i(2))), db.exec("select id from names order by id desc limit 1"))
        assertEquals(listOf(row(i(1))), db.exec("select id from names order by id desc limit 1 offset 1"))

        assertEquals(empty, db.exec("create index id on names (id)"))
        assertEquals(listOf(row(s("id"))), db.exec("show index from names"))
        assertEquals(listOf(row(s("John"))), db.exec("select value from names where id = 1"))
        assertEquals(listOf(row(s("Jane"))), db.exec("select value from names where id = 2"))
        assertEquals(listOf(row(s("Jane"))), db.exec("select value from names where id != 1"))

        assertEquals(empty, db.exec("update names set id = 4 where id = 1"))
        assertEquals(listOf(row(s("John"))), db.exec("select value from names where id = 4"))
        assertEquals(empty, db.exec("select value from names where id = 1"))

        assertEquals(empty, db.exec("delete from names where id = 4"))
        assertEquals(empty, db.exec("select value from names where id = 1"))

        assertEquals(empty, db.exec("delete from names"))
        assertEquals(empty, db.exec("select * from names"))

        assertEquals(empty, db.exec("drop table names"))
        assertEquals(empty, db.exec("show tables"))
    }

    @Test
    fun script() {
        val db = DB()
        val sql =
            """
            create table names(id integer, value string); -- Create the table
            insert into names values(1, "John");  -- Insert a value
            select * from names;  -- Show the table contents
            """.trimIndent()

        val results = db.script(sql)

        assertEquals(
            listOf(
                empty,
                empty,
                listOf(row(i(1), s("John")))
            ),
            results
        )
    }

    private fun row(vararg values: Value): Row = Row(values.toMutableList())
    private fun s(value: String): Value = StringValue(value)
    private fun i(value: Int): Value = IntValue(value)
}
