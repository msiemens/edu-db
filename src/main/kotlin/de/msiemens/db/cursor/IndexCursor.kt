package de.msiemens.db.cursor

// class IndexCursor<T : Comparable<T>>(
//    val index: BinaryTree<T, List<Int>>,
//    val start: Int? = null,
//    val stop: Int? = null
// ) : Cursor<Int> {
//    private val stack: ArrayDeque<BinaryTree.Node<T, List<Int>>> = ArrayDeque()
//
//    init {
//        var current = index.root()
//
//        while (current != null) {
//            stack.add(current)
//            current = current.left
//        }
//    }
//
//    override fun next(): Int {
//        val current = stack.removeLastOrNull() ?: error("Cursor.next() called on an ended index cursor")
//
//        var next = current.right
//
//        while (next != null) {
//            stack.add(next)
//            next = next.left
//        }
//
//        return current.value
//    }
//
//    override fun end(): Boolean = stack.isEmpty()
// }
