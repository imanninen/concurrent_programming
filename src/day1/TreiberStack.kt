package day1

import kotlinx.atomicfu.*

class TreiberStack<E> : Stack<E> {
    // Initially, the stack is empty.
    private val top = atomic<Node<E>?>(null)

    override fun push(element: E) {
        // TODO: Make me linearizable!
        // TODO: Update `top` via Compare-and-Set,++
        // TODO: restarting the operation on CAS failure.
        var curTop = top.value
        var newTop = Node(element, curTop)
        while(!top.compareAndSet(curTop, newTop)){
            curTop = top.value
            newTop = Node(element, curTop)
        }
    }

    override fun pop(): E? {
        // TODO: Make me linearizable!
        // TODO: Update `top` via Compare-and-Set,
        // TODO: restarting the operation on CAS failure.
        var curTop = top.value ?: return null

        while(!top.compareAndSet(curTop, curTop.next.value)){
            curTop = top.value ?: return null
        }
        return curTop.element
    }

    private class Node<E>(
        val element: E,
        next: Node<E>?
    ) {
        val next = atomic(next)
    }
}