package day4

import kotlinx.atomicfu.*

// This implementation never stores `null` values.
class DynamicArray<E: Any> {
    private val core = atomic(Core(capacity = 1)) // Do not change the initial capacity

    /**
     * Adds the specified [element] to the end of this array.
     */
    fun addLast(element: E) {
        // TODO: Implement me!
        // TODO: Yeah, this is a hard task, I know ...
        while (true) {
            val currentCore = core.value
            val nextCore = currentCore.next.value
            if (nextCore == null) {
                val currentSize = currentCore.size.value
                if (currentSize == currentCore.capacity) {
                    val newCore = Core(currentCore.capacity * 2)
                    newCore.size.value = currentSize
                    currentCore.next.compareAndSet(null, newCore)
                    continue
                }

                if (!currentCore.array[currentSize].compareAndSet(null, element)) {
                    currentCore.size.compareAndSet(currentSize, currentSize+1)
                    continue
                }
                while (true) {
                    currentCore.size.compareAndSet(currentSize, currentSize + 1)
                    if (currentCore.size.value > currentSize) return
                }
            }
            else {
                var index = 0
                while (index < currentCore.size.value) {
                    when (val curElement = currentCore.array[index].value) {
                        is Frozen -> {
                            nextCore.array[index].compareAndSet(null, curElement.element)
                            index++
                            continue
                        }
                        else -> {
                            currentCore.array[index].compareAndSet(curElement, Frozen(curElement!!))
                            continue
                        }
                    }
                }
                core.compareAndSet(currentCore, nextCore)
            }
        }
    }

    /**
     * Puts the specified [element] into the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the size of this array.
     */
    fun set(index: Int, element: E) {
        while (true) {
            val currentCore = core.value
            val currentSize = currentCore.size.value
            require(index < currentSize) { "index must be lower than the array size" }
            // TODO: check that the cell is not "frozen"

            when (val curCellValue = currentCore.array[index].value) {
                is Frozen -> {
                    val nextCore = currentCore.next.value!!
                    var copyIndex = 0
                    while (copyIndex < currentCore.size.value) {
                        when (val curElement = currentCore.array[copyIndex].value) {
                            is Frozen -> {
                                nextCore.array[copyIndex].compareAndSet(null, curElement.element)
                                copyIndex++
                                continue
                            }
                            else -> {
                                if (currentCore.array[copyIndex].compareAndSet(curElement, Frozen(curElement!!))) {
                                    nextCore.array[copyIndex].compareAndSet(null, curElement)
                                    copyIndex++
                                    continue
                                }
                                else continue
                            }
                        }
                    }
                    core.compareAndSet(currentCore, nextCore)
                    continue
                }

                else -> {
                    if (!currentCore.array[index].compareAndSet(curCellValue, element)) continue
                    return
                }
            }
        }
    }

    /**
     * Returns the element located in the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the size of this array.
     */
    @Suppress("UNCHECKED_CAST")
    fun get(index: Int): E {
        val currentCore = core.value
        val currentSize = currentCore.size.value
        require(index < currentSize) { "index must be lower than the array size" }
        // TODO: check that the cell is not "frozen",
        // TODO: unwrap the element in this case.

        return when(val curCellValue = currentCore.array[index].value) {
            is Frozen -> {
                curCellValue.element as E
            }

            else -> {
                curCellValue as E
            }
        }
    }

    @JvmInline
    private value class Frozen(val element: Any)

    private class Core(
        val capacity: Int
    ) {
        val array = atomicArrayOfNulls<Any?>(capacity)
        val size = atomic(0)
        val next = atomic<Core?>(null)
    }
}