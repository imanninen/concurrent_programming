package day2

import day1.*
import kotlinx.atomicfu.*

class FAABasedQueueSimplified<E> : Queue<E> {
    private val infiniteArray = atomicArrayOfNulls<Any?>(1024) // conceptually infinite array
    private val enqIdx = atomic(0)
    private val deqIdx = atomic(0)

    override fun enqueue(element: E) {

//        // TODO: Increment the counter atomically via Fetch-and-Add.
//        // TODO: Use `getAndIncrement()` function for that.
//        val i = enqIdx.value
//        enqIdx.value = i + 1
//        // TODO: Atomically install the element into the cell
//        // TODO: if the cell is not poisoned.
//        infiniteArray[i].value = element
        while (true){
            val index = enqIdx.getAndIncrement()
            if (infiniteArray[index].compareAndSet(null, element))
                return
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun dequeue(): E? {
        // Is this queue empty?
//        if (enqIdx.value <= deqIdx.value) return null
//        // TODO: Increment the counter atomically via Fetch-and-Add.
//        // TODO: Use `getAndIncrement()` function for that.
//        val i = deqIdx.value
//        deqIdx.value = i + 1
//        // TODO: Try to retrieve an element if the cell contains an
//        // TODO: element, poisoning the cell if it is empty.
//        return infiniteArray[i].value as E
        while (true){
            if (deqIdx.value >= enqIdx.value) return null
            val index = deqIdx.getAndIncrement()
            if (infiniteArray[index].compareAndSet(null, POISONED)) continue
            return infiniteArray[index].value as E?
        }
    }
}

// TODO: poison cells with this value.
private val POISONED = Any()
