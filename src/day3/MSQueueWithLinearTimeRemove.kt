@file:Suppress("FoldInitializerAndIfToElvis", "DuplicatedCode")

package day3

import day1.MSQueue
import kotlinx.atomicfu.*

class MSQueueWithLinearTimeRemove<E> : QueueWithRemove<E> {
    private val head: AtomicRef<Node>
    private val tail: AtomicRef<Node>

    init {
        val dummy = Node(null)
        head = atomic(dummy)
        tail = atomic(dummy)
    }

    override fun enqueue(element: E) {
        // TODO: When adding a new node, check whether
        // TODO: the previous tail is logically removed.
        // TODO: If so, remove it physically from the linked list.
        //        TODO("Implement me!")
        while (true) {
            val node = Node(element)
            val curTail = tail.value

            if (curTail.next.compareAndSet(null, node))
            {
                tail.compareAndSet(curTail, node)

                if (curTail.extractedOrRemoved) {
                    curTail.remove()
                }

                return
            }
            else
            {
                val curTailNext = curTail.next.value ?: continue
                tail.compareAndSet(curTail, curTailNext)

                if (curTail.extractedOrRemoved)
                    curTail.remove()
            }
        }
    }

    override fun dequeue(): E? {
        // TODO: After moving the `head` pointer forward,
        // TODO: mark the node that contains the extracting
        // TODO: element as "extracted or removed", restarting
        // TODO: the operation if this node has already been removed.

        while (true) {
            val curHead = head.value
            val curHeadNext = curHead.next.value ?: return null

            if (!curHeadNext.markExtractedOrRemoved()) {

                // check if we are after tail
                var temp = tail.value
                while (true) {
                    if (temp == curHeadNext) return null
                    temp = temp.next.value ?: break
                }

                curHeadNext.remove()
                continue
            }

            head.compareAndSet(curHead, curHeadNext)
            return curHeadNext.element

        }
    }

    override fun remove(element: E): Boolean {
        // Traverse the linked list, searching the specified
        // element. Try to remove the corresponding node if found.
        // DO NOT CHANGE THIS CODE.
        var node = head.value
        while (true) {
            val next = node.next.value
            if (next == null) return false
            node = next
            if (node.element == element && node.remove()) return true
        }
    }

    /**
     * This is an internal function for tests.
     * DO NOT CHANGE THIS CODE.с
     */
    override fun checkNoRemovedElements() {
        var node = head.value
        // Traverse the linked list
        while (true) {
            if (node !== head.value && node !== tail.value) {
                check(!node.extractedOrRemoved) {
                    "Removed node with element ${node.element} found in the middle of this queue"
                }
            }
            node = node.next.value ?: break
        }
    }

    /**
     * This is a string representation of the data structure,
     * you see it in Lincheck tests when they fail.
     * DO NOT CHANGE THIS CODE.
     */
    override fun toString(): String {
        // Choose the leftmost node.
        var node = head.value
        if (tail.value.next.value === node) {
            node = tail.value
        }
        // Traverse the linked list.
        val nodes = arrayListOf<String>()
        while (true) {
            nodes += (if (head.value === node) "HEAD = " else "") +
                    (if (tail.value === node) "TAIL = " else "") +
                    "<${node.element}" +
                    (if (node.extractedOrRemoved) ", extractedOrRemoved" else "") +
                    ">"
            // Process the next node.
            node = node.next.value ?: break
        }
        return nodes.joinToString(", ")
    }

    // TODO: Node is an inner class for accessing `head` in `remove()`
    private inner class Node(
        var element: E?
    ) {
        val next = atomic<Node?>(null)

        /**
         * TODO: Both [dequeue] and [remove] should mark
         * TODO: nodes as "extracted or removed".
         */
        private val _extractedOrRemoved = atomic(false)
        val extractedOrRemoved = _extractedOrRemoved.value

        fun markExtractedOrRemoved(): Boolean = _extractedOrRemoved.compareAndSet(false, true)

        /**
         * Removes this node from the queue structure.
         * Returns `true` if this node was successfully
         * removed, or `false` if it has already been
         * removed by [remove] or extracted by [dequeue].
         */
        fun remove(): Boolean {
            // TODO: The removal procedure is split into two phases.
            // TODO: First, you need to mark the node as "extracted or removed".
            // TODO: On success, this node is logically removed, and the
            // TODO: operation should return `true` at the end.
            // TODO: In the second phase, the node should be removed
            // TODO: physically, updating the `next` field of the previous
            // TODO: node to `this.next.value`.
            // TODO: Do not remove `head` and `tail` physically to make
            // TODO: the algorithm simpler. In case a tail node is logically removed,
            // TODO: it will be removed physically by `enqueue(..)`.
            // TODO("Implement me!")

            val mark = this.markExtractedOrRemoved()

            if (head.value == this) return mark // Don't delete first element

            var curPrev = head.value

            while (true)
            {
                val curPrevNext = curPrev.next.value
                if (curPrevNext == null) return mark // there is no such element
                if (curPrevNext == this) break // found it

                curPrev = curPrevNext
            }

            val curNext = next.value
            if (curNext == null) return mark // Don't delete the last element

            curPrev.next.value = curNext

            if (head.value != curPrev && curPrev.extractedOrRemoved) curPrev.remove()
            if (tail.value != curNext && curNext.extractedOrRemoved) curNext.remove()

            return mark
        }
    }
}