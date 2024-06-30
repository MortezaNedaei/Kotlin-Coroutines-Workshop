package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

fun main(): Unit = runBlocking {

    lifeCycleColdFlow()
    lifeCycleHotFlow()
}

fun lifeCycleColdFlow() = runBlocking {
    val flow = flow {
        delay(2000)
        emit("item 1")
        delay(2000)
    }
    flow
        .onStart { println("LifeCycle: onStart") } // When onStart is invoked? when the consumer requests to collect values from producer. Means Exactly before this flow starts to be collected.
        .onEach { println("LifeCycle: onEach") } // Exactly before emitting values from producer to consumer. The best method to apply a transform function to each value emitted by the flow
        .onCompletion { println("LifeCycle: onCompletion") } // equivalent to `finally {}` in try catch block
        .collect() { println(it) } // requests to producer to collect data
}

/**
 * SharedFlow never completes. Means onCompletion is not triggered normally. But you can use `takeWhile {}` or `take()` to complete it by cancelling it.
 */
fun lifeCycleHotFlow() = runBlocking {
    val hotFlow = MutableSharedFlow<Int>(replay = 3, onBufferOverflow = BufferOverflow.SUSPEND)
    hotFlow.tryEmit(1) // emit the initial value
    hotFlow.tryEmit(2)
    hotFlow.tryEmit(3)

    hotFlow
        .onStart { println("LifeCycle: onStart") }
        .onEach { println("LifeCycle: onEach") }
        .onCompletion { println("LifeCycle: onCompletion") } // Shared flow never completes. A call to `collect {}` on a shared flow never completes normally. Use `takeWhile {}` or `take()` to complete it
        .take(2) // Shared flow never completes. Means `onCompletion` is not triggered until using `takeWhile` or `take()`
        .takeWhile { it != 3 } // Shared flow never completes. Means `onCompletion` is not triggered until using `takeWhile` or `take()`
        .collect() { println(it) }
}
