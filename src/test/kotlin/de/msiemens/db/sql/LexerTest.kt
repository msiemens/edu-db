package de.msiemens.db.sql

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class LexerTest {
    @Test
    operator fun next() {
        val lexer = Lexer("show tables")
        assertEquals(lexer.next(), Token(Token.Type.SHOW))
        assertEquals(lexer.next(), Token(Token.Type.TABLES))
        assertEquals(lexer.next(), Token(Token.Type.EOF))
    }
}
