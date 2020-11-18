package de.msiemens.db.sql

import de.msiemens.db.query.Condition
import de.msiemens.db.query.Operator
import de.msiemens.db.sql.statements.*
import de.msiemens.db.table.*

class Parser(input: String) {
    private val lexer = Lexer(input)

    fun parseScript(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!eat(Token.Type.EOF)) {
            statements.add(parseStatement())
        }

        return statements
    }

    fun parseStatement(): Statement {
        val token = lexer.next()

        return when (token.type) {
            Token.Type.CREATE -> parseCreate()
            Token.Type.INSERT -> parseInsert()
            Token.Type.SELECT -> parseSelect()
            Token.Type.UPDATE -> parseUpdate()
            Token.Type.DELETE -> parseDelete()
            Token.Type.SHOW -> parseShow()
            Token.Type.DROP -> parseDrop()
            else -> error("Unexpected token: $token")
        }
    }

    private fun parseCreate(): Statement {
        val token = lexer.next()

        return when (token.type) {
            Token.Type.TABLE -> parseCreateTable()
            Token.Type.INDEX -> parseCreateIndex()
            else -> error("Unexpected token: $token")
        }
    }

    private fun parseCreateTable(): Statement {
        val tableName = parseString()
        val columns = mutableListOf<ColumnDefinition>()

        expect(Token.Type.PAREN_LEFT)

        do {
            val columnName = parseString()
            val type = parseColumnType()

            columns.add(ColumnDefinition(columnName, type))
        } while (eat(Token.Type.COMMA))

        expect(Token.Type.PAREN_RIGHT)
        end()

        return CreateTableStatement(tableName, columns)
    }

    private fun parseCreateIndex(): Statement {
        val indexName = parseString()

        expect(Token.Type.ON)

        val tableName = parseString()

        expect(Token.Type.PAREN_LEFT)

        val columnName = parseString()

        expect(Token.Type.PAREN_RIGHT)
        end()

        return CreateIndexStatement(indexName, tableName, columnName)
    }

    private fun parseInsert(): Statement {
        expect(Token.Type.INTO)

        val tableName = parseString()
        val values = mutableListOf<Value>()

        expect(Token.Type.VALUES)
        expect(Token.Type.PAREN_LEFT)

        do {
            values.add(parseValue())
        } while (eat(Token.Type.COMMA))

        expect(Token.Type.PAREN_RIGHT)
        end()

        return InsertStatement(tableName, Row(values))
    }

    private fun parseSelect(): Statement {
        var columns: MutableList<String>? = null

        if (!eat(Token.Type.STAR)) {
            columns = mutableListOf()
            do {
                val name = parseString()

                columns.add(name)
            } while (eat(Token.Type.COMMA))
        }

        expect(Token.Type.FROM)

        val tableName = parseString()

        val query = if (eat(Token.Type.WHERE)) {
            parseQuery()
        } else {
            null
        }

        val order = if (eat(Token.Type.ORDER)) {
            parseOrderBy()
        } else {
            null
        }

        val (limit, offset) = if (eat(Token.Type.LIMIT)) {
            val offset = parseInt()

            if (eat(Token.Type.OFFSET)) {
                val limit = parseInt()

                (offset to limit)
            } else {
                (offset to null)
            }
        } else {
            null to null
        }

        end()

        return SelectStatement(tableName, columns, query, order, limit, offset)
    }

    private fun parseUpdate(): Statement {
        val tableName = parseString()

        expect(Token.Type.SET)

        val values = mutableListOf<Pair<String, Value>>()

        do {
            val columnName = parseString()

            expect(Token.Type.OP_EQ)

            val value = parseValue()

            values.add(columnName to value)
        } while (eat(Token.Type.COMMA))

        val query = if (eat(Token.Type.WHERE)) {
            parseQuery()
        } else {
            null
        }

        end()

        return UpdateStatement(tableName, values, query)
    }

    private fun parseDelete(): Statement {
        expect(Token.Type.FROM)

        val tableName = parseString()

        val query = if (eat(Token.Type.WHERE)) {
            parseQuery()
        } else {
            null
        }

        end()

        return DeleteStatement(tableName, query)
    }

    private fun parseDrop(): Statement {
        expect(Token.Type.TABLE)

        val tableName = parseString()

        end()

        return DropTableStatement(tableName)
    }

    private fun parseOrderBy(): Pair<String, OrderDirection> {
        expect(Token.Type.BY)

        val orderColumnName = parseString()

        val orderDirection = if (eat(Token.Type.ORDER_ASC)) {
            OrderDirection.ASC
        } else {
            expect(Token.Type.ORDER_DESC)
            OrderDirection.DESC
        }

        return orderColumnName to orderDirection
    }

    private fun parseShow(): Statement {
        val token = lexer.next()
        when (token.type) {
            Token.Type.TABLES -> {
                end()

                return ShowTablesStatement
            }
            Token.Type.INDEX -> {
                expect(Token.Type.FROM)

                val tableName = parseString()

                end()

                return ShowIndexStatement(tableName)
            }
            else -> error("Invalid show statement $token")
        }
    }

    private fun parseQuery(): Condition {
        val (name, op, value) = parseSingleQuery()

        val andClauses = mutableListOf<Condition>()
        while (eat(Token.Type.AND)) {
            andClauses.add(parseSingleQuery())
        }

        val orClauses = mutableListOf<Condition>()
        while (eat(Token.Type.OR)) {
            orClauses.add(parseQuery())
        }

        return Condition(name, op, value, orClauses, andClauses)
    }

    private fun parseSingleQuery(): Condition {
        if (eat(Token.Type.PAREN_LEFT)) {
            val query = parseQuery()
            expect(Token.Type.PAREN_RIGHT)

            return query
        }

        val name = parseString()
        val token = lexer.next()
        val op = when (token.type) {
            Token.Type.OP_EQ -> Operator.EQ
            Token.Type.OP_NE -> Operator.NE
            Token.Type.OP_GT -> Operator.GT
            Token.Type.OP_GE -> Operator.GE
            Token.Type.OP_LT -> Operator.LT
            Token.Type.OP_LE -> Operator.LE
            Token.Type.LIKE -> Operator.LIKE
            else -> error("Invalid operator $token")
        }
        val value = parseValue()

        return Condition(name, op, value)
    }

    private fun parseValue(): Value {
        val token = lexer.next()

        return when (token.type) {
            Token.Type.VAL_STRING -> StringValue(token.value ?: error("String token no string value"))
            Token.Type.VAL_INT -> IntValue(Integer.parseInt(token.value ?: error("String token no string value")))
            else -> error("$token is not a value")
        }
    }

    private fun parseString(): String = expect(Token.Type.VAL_STRING).value ?: error("String token no string value")

    private fun parseInt(): Int = Integer.parseInt(expect(Token.Type.VAL_INT).value ?: error("Int token no string value"))

    private fun parseColumnType(): ColumnType {
        val token = lexer.next()

        return when (token.type) {
            Token.Type.STRING -> ColumnType.STRING
            Token.Type.INTEGER -> ColumnType.INT
            else -> error("$token is not a column type")
        }
    }

    private fun expect(type: Token.Type): Token {
        val token = lexer.next()
        if (token.type != type) {
            error("Unexpected token: $token")
        }

        return token
    }

    private fun eat(type: Token.Type): Boolean {
        val token = lexer.peek()
        if (token.type == type) {
            lexer.consume()

            return true
        }

        return false
    }

    private fun end() {
        val token = lexer.next()

        when (token.type) {
            Token.Type.EOF -> return
            Token.Type.SEMICOLON -> return
            else -> error("Unexpected token: $token")
        }
    }
}
