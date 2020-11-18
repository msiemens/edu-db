package de.msiemens.db.index

import de.msiemens.db.data.BinaryTree
import de.msiemens.db.query.Condition
import de.msiemens.db.query.Operator
import de.msiemens.db.table.Value

class Index<T : Value> {
    private var storage: BinaryTree<T, Set<Int>> = BinaryTree()

    fun fill(rows: List<Pair<Int, T>>) {
        storage.fill(rows.map { it.second to setOf(it.first) })
    }

    fun add(value: T, idx: Int) {
        val entry = storage.find(value)
        if (entry != null) {
            entry.value += idx
        } else {
            storage.insert(value, setOf(idx))
        }
    }

    fun remove(value: T, idx: Int) {
        val entry = storage.find(value) ?: return

        entry.value -= idx

        if (entry.value.isEmpty()) {
            storage.remove(value)
        }
    }

    fun cursor(condition: Condition): List<Int> {
        return cursor(storage.root(), condition).toList()
    }

    private fun cursor(current: BinaryTree.Node<T, Set<Int>>?, condition: Condition): Set<Int> {
        if (current == null) {
            return emptySet()
        }

        val value = condition.value
        val op = condition.operator

        if (value == current.key) {
            return when (op) {
                Operator.EQ -> current.value
                Operator.NE -> indices(current.left) + indices(current.right)
                Operator.GT -> all(current.right)
                Operator.GE -> current.value + all(current.right)
                Operator.LT -> all(current.left)
                Operator.LE -> current.value + all(current.left)
                Operator.LIKE -> error("LIKE condition not supported with index")
            }
        }

        return if (value < current.key) {
            when (op) {
                Operator.EQ, Operator.LT, Operator.LE -> cursor(current.left, condition)
                Operator.GT, Operator.GE -> all(current.right) + indices(current) + cursor(current.left, condition)
                Operator.NE -> cursor(current.left, condition) + indices(current) + all(current.right)
                Operator.LIKE -> error("LIKE condition not supported with index")
            }
        } else {
            when (op) {
                Operator.EQ, Operator.GT, Operator.GE -> cursor(current.right, condition)
                Operator.LT, Operator.LE -> all(current.left) + indices(current) + cursor(current.right, condition)
                Operator.NE -> cursor(current.right, condition) + indices(current) + all(current.left)
                Operator.LIKE -> error("LIKE condition not supported with index")
            }
        }
    }

    private fun indices(node: BinaryTree.Node<T, Set<Int>>?): Set<Int> = node?.value ?: emptySet()

    private fun all(node: BinaryTree.Node<T, Set<Int>>?): Set<Int> {
        if (node == null) {
            return emptySet()
        }

        return node.value + all(node.left) + all(node.right)
    }

//    fun cursor(start: Int?, stop: Int?): Cursor<Int> = IndexCursor(storage, start, stop)
}
