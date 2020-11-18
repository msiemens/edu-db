package de.msiemens.db.sql

import de.msiemens.db.query.Condition
import de.msiemens.db.query.Operator
import de.msiemens.db.sql.statements.SelectStatement
import de.msiemens.db.table.IntValue
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class ParserTest {
    @Test
    fun parse() {
        val stmt = Parser("select * from names where a = 4 and a = 5 or a = 6 and a = 7").parseStatement()
        assertTrue(stmt is SelectStatement)

        val select = stmt as SelectStatement

        assertNull(select.columns)
        assertNull(select.order)
        assertEquals(select.table, "names")
        assertEquals(
            select.condition,
            Condition(
                "a",
                Operator.EQ,
                i(4),
                orClauses = listOf(
                    Condition(
                        "a",
                        Operator.EQ,
                        i(6),
                        andClauses = listOf(
                            Condition(
                                "a",
                                Operator.EQ,
                                i(7),
                            )
                        )
                    )
                ),
                andClauses = listOf(
                    Condition(
                        "a",
                        Operator.EQ,
                        i(5),
                    )
                )
            )
        )
    }

    private fun i(value: Int): IntValue = IntValue(value)
}
