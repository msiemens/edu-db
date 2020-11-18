package de.msiemens.db.cursor

interface Cursor<T> {
    fun next(): T

    fun end(): Boolean
}
