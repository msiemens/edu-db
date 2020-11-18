package de.msiemens.db.table

sealed class Value : Comparable<Value> {
    abstract val type: ColumnType
}

data class StringValue(val value: String) : Value() {
    override val type: ColumnType
        get() = ColumnType.STRING

    override fun compareTo(other: Value): Int = value.compareTo((other as StringValue).value)
}

data class IntValue(val value: Int) : Value() {
    override val type: ColumnType
        get() = ColumnType.INT

    override fun compareTo(other: Value): Int = value.compareTo((other as IntValue).value)
}
