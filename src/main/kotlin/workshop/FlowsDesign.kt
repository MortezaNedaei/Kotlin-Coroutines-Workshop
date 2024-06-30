package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * A flow builder does not run until the flow is collected. Means flow needs to call a terminal operator to emit events.
 * But how does this work internally?
 *
 */
fun main() = runBlocking {
    flow1()
    flow2()
    runHigherOrderFunction()
}

/**
 * 1. When you pass suspending lambda block which emits values that — ranges from 1 to 5 — to flow { } builder, an object called SafeFlow is created and it has the suspending lambda block as a member peoperty.
 *
 * flow{} builder source code:
 *
 * public fun <T> flow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)
 *
 * 2. When you call `collect { value -> … }` function to collect data from the Flow, the lambda block {value -> …} for collecting is wrapped as a form of FlowCollector (orange colored).
 * After that it is wrapped again as the SafeCallector object (purple colored) which is composed of two property objects: coroutine context of the collector and the FlowCollector.
 *
 * 3. Finally, the suspending function which is created in step 1 is called in order to emit its values and the receiver is SafeCollector which is created in the step 2.
 *
 * SafeFlow source code:
 *
 * private class SafeFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : AbstractFlow<T>() {
 *     override suspend fun collectSafely(collector: FlowCollector<T>) {
 *       // collector is "SafeCollector"
 *       // block is "data emission block
 *         collector.block()
 *     }
 * }
 *
 * The “block” property is data emitting function that is passed from the flow { } builder (explained in step 1) and the “collector” parameter of collectSafely function is SafeCollector (explained in step 2).
 * When data emission block is executed, the emit functions (emit(1),…,emit(5)) starts to send value sequentially to the SafeCollector.
 * As a Result, SafeCollector mediate the data from the emitter to the suspend lambda block which is passed from collect() function.
 *
 */
private fun flow1() = runBlocking {
    flow {
        repeat(5) {
            emit(it)
        }
    }.collect { value -> //equivalent to flowOf("").collect(object : FlowCollector<String> { override suspend fun emit(value: String) {} })
        println(value)
    }
}

private fun flow2() = runBlocking {
    flow(object: suspend (FlowCollector<Int>) -> Unit { // Higher Order Function that returns a FlowCollector
        override suspend fun invoke(collector: FlowCollector<Int>) {
            repeat(5) {
                collector.emit(it)
            }
        }

    }).collect(object : FlowCollector<Int> {
        override suspend fun emit(value: Int) {
            println(value)
        }
    })
}

fun runHigherOrderFunction() {
    higherOrderFunction1 { value: String ->
        println(value)
    }
    // or
    higherOrderFunction1(object : (String) -> Unit {
        override fun invoke(value: String) {
            println(value)
        }
    })
    higherOrderFunction2 { // You are inside the String !
        println(length)
    }
}


/**
 * Simple Higher Order Function which gives a String and returns a Unit
 */
private fun higherOrderFunction1(callback: (String) -> Unit) {
    callback("Hello")
        // or
    callback.invoke("Hello")
}

/**
 * Similar to [higherOrderFunction1], but uses [ExtensionFunctionType] HOF instead of [FunctionType] HOF.
 */
private fun higherOrderFunction2(callback: String.() -> Unit) {
    callback("Hello")
        // or
    callback.invoke("Hello")
}
