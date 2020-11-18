package de.msiemens.db.data

class BinaryTree<K : Comparable<K>, V> {
    data class Node<K, V>(
        var key: K,
        var value: V,

        internal var left: Node<K, V>? = null,
        internal var right: Node<K, V>? = null
    )

    private var root: Node<K, V>? = null

    fun root() = root

    fun insert(key: K, value: V) {
        insert(root, key, value)
    }

    fun find(key: K): Node<K, V>? {
        var current = root ?: return null

        while (true) {
            when {
                current.key == key -> return current
                key < current.key -> current = current.left ?: return null
                key > current.key -> current = current.right ?: return null
            }
        }

        // If the loop never ends, our tree has cycles...
    }

    fun remove(value: K) {
        root = remove(value, root)
    }

    fun fill(rows: List<Pair<K, V>>) {
        root = build(rows.sortedBy { it.first })
    }

    private fun insert(node: Node<K, V>?, key: K, value: V): Node<K, V> {
        if (node == null) {
            return Node(key, value)
        }

        when {
            key < node.key -> {
                node.left = insert(node.left, key, value)
            }
            key > node.key -> {
                node.right = insert(node.right, key, value)
            }
            key == node.key -> {
                node.value = value
            }
        }

        return node
    }

    private fun remove(key: K, node: Node<K, V>?): Node<K, V>? {
        if (node == null) {
            return node
        }

        val right = node.right
        val left = node.left

        when {
            key < node.key -> {
                node.left = remove(key, left)
            }
            key > node.key -> {
                node.right = remove(key, right)
            }
            key == node.key -> {
                if (left == null) {
                    return right
                } else if (right == null) {
                    return left
                }

                // Node with two children: Get the inorder successor
                // (smallest in the right subtree)
                val minNode = min(right)

                node.key = minNode.key
                node.value = minNode.value
                node.right = remove(key, right)
            }
        }

        return node
    }

    private fun min(node: Node<K, V>): Node<K, V> {
        var current: Node<K, V> = node

        while (true) {
            current = current.left ?: return current
        }
    }

    private fun build(rows: List<Pair<K, V>>): Node<K, V>? {
        if (rows.isEmpty()) {
            return null
        }

        val mid = rows.size / 2
        val value = rows[mid]

        val node = Node(value.first, value.second)

        node.left = build(rows.subList(0, mid))
        node.right = build(rows.subList(mid + 1, rows.size))

        return node
    }
}
