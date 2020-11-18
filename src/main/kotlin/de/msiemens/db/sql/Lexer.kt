package de.msiemens.db.sql

class Lexer(private val input: String) {
    private var pos: Int = 0
    private var peek: Token? = null

    fun next(): Token {
        var token = peek
        if (token != null) {
            peek = null

            return token
        }

        while (!eof()) {
            token = readToken()
            if (token != null) {
                return token
            }
        }

        return Token(Token.Type.EOF)
    }

    fun peek(): Token {
        var token = peek

        if (token != null) {
            return token
        }

        while (!eof()) {
            token = readToken()
            if (token != null) {
                peek = token

                return token
            }
        }

        return Token(Token.Type.EOF)
    }

    fun consume() {
        next()
    }

    private fun eof(): Boolean = pos >= input.length

    private fun readToken(): Token? {
        val remainder = input.substring(pos)

        if (input[pos].isWhitespace() || input[pos] == '\n') {
            pos += 1

            return null
        }

        if (remainder.startsWith("--")) {
            skipLine()

            return null
        }

        // Parse keywords
        for (type in Token.Type.values()) {
            if (type.token == null) {
                continue
            }

            if (remainder.startsWith(type.token)) {
                pos += type.token.length

                return Token(type)
            }
        }

        val int = tokenizeInt()
        if (int != null) {
            return int
        }

        val string = tokenizeString()
        if (string != null) {
            return string
        }

        error("Unexpected character: `${input[pos]}`")
    }

    private fun tokenizeInt(): Token? {
        val start = pos
        while (!eof() && input[pos].isDigit()) {
            pos += 1
        }

        if (start != pos) {
            return Token(Token.Type.VAL_INT, input.substring(start, pos))
        }

        return null
    }

    private fun tokenizeString(): Token? {
        val start = pos

        if (eat('"')) {
            return tokenizeQuotedString()
        }

        while (!eof() && input[pos].isLetterOrDigit()) {
            pos += 1
        }

        if (start != pos) {
            return Token(Token.Type.VAL_STRING, input.substring(start, pos))
        }

        return null
    }

    private fun tokenizeQuotedString(): Token {
        val start = pos

        while (!eof() && input[pos] != '"') {
            pos += 1
        }

        val value = input.substring(start, pos)

        pos += 1 // Skip the closing quote

        return Token(Token.Type.VAL_STRING, value)
    }

    private fun skipLine() {
        while (!eof() && input[pos] != '\n') {
            pos += 1
        }
    }

    private fun eat(char: Char): Boolean {
        if (input[pos] == char) {
            pos += 1

            return true
        }

        return false
    }
}
