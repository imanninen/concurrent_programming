package day4

import kotlinx.atomicfu.*

// This implementation never stores `null` values.
class DynamicArraySimplified<E : Any>(
    private val capacity: Int
) {
    private val array = atomicArrayOfNulls<Any?>(capacity)
    private val size = atomic(0) // never decreases

    fun addLast(element: E): Boolean {
        while (true) {
            val currentSize = size.value
            if (currentSize == capacity) return false
            // TODO: you need to install the element and
            // TODO: increment the size atomically.
            // TODO: You are NOT allowed to use CAS2,
            // TODO: there is a more efficient and smarter solution!

            if (!array[currentSize].compareAndSet(null, element)) {
                size.compareAndSet(currentSize, currentSize+1)
                continue
            }
            while (true) {
                size.compareAndSet(currentSize, currentSize + 1)
                if (size.value > currentSize) return true
            }
        }
    }

    fun set(index: Int, element: E) {
        val currentSize = size.value
        require(index < currentSize) { "index must be lower than the array size" }
        // As the size never decreases, this update is safe.
        array[index].value = element
    }

    @Suppress("UNCHECKED_CAST")
    fun get(index: Int): E {
        val currentSize = size.value
        require(index < currentSize) { "index must be lower than the array size" }
        // As the size never decreases, this read is safe.
        return array[index].value as E
    }
}