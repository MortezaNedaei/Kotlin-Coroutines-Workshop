package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() {

    // 1. Context Preservation
    contextPreservationException()
    contextPreservationSolution()

    // 2. Exception Transparency
    exceptionTransparencyIssue()
    exceptionTransparencySolution()
}

/**
 * According to context execution preservation You can't use `withContext` inside a flow to change the coroutine context in which an item is emitted.
 * The execution context of a flow refers to the coroutine context in which the flow was created and determines the thread pool and dispatcher that are used for the execution of each flow and you shouldn't change it's internal behavior using `withContext`.
 * To fix issue use [flowOn] instead to change the dispatcher of the upstream emitter.
 * When using the `flowOn` operator to change the dispatcher of the upstream emitter in a flow, a [ChannelCoroutine] is added in the middle of the collector and the flow.
 * This [ChannelCoroutine] has the specified dispatcher as its coroutine context element.
 * When calling the `flow.collect { }`, data is received from this channel, ensuring that the downstream collector runs in the specified coroutine context while preserving the execution context of the flow.
 * This enables you to control the coroutine context in which downstream processing occurs while ensuring that the flow remains exception-transparent and concurrency-safe.
 * @see contextPreservationSolution
 */
private fun contextPreservationException() = runBlocking {
    flow {
        withContext(Dispatchers.Default) { // throws java.lang.IllegalStateException: Flow invariant is violated: Flow was collected in [BlockingCoroutine{Active}@6cb14a61, BlockingEventLoop@d50fac3], but emission happened in [DispatchedCoroutine{Active}@5df14437, Dispatchers.Default].
            emit("1")
        }
    }.collect()
}

/**
 * When using the `flowOn` operator to change the dispatcher of the upstream emitter in a flow, a [ChannelCoroutine] is added in the middle of the collector and the flow.
 * This [ChannelCoroutine] has the specified dispatcher as its coroutine context element.
 * When calling the `flow.collect { }`, data is received from this channel, ensuring that the downstream collector runs in the specified coroutine context while preserving the execution context of the flow.
 * This enables you to control the coroutine context in which downstream processing occurs while ensuring that the flow remains exception-transparent and concurrency-safe.
 */
private fun contextPreservationSolution() = runBlocking {
    flow { emit("1") }
        .flowOn(Dispatchers.Default)
        .collect { println("contextPreservationSolution: collecting on Dispatchers.Default : $it") }
}


/**
 * Exception transparency refers to the ability of a program to correctly handle exceptions in a transparent and predictable manner, without compromising the correctness or safety of the program.
 * In other words, exceptions should be able to propagate through code in a way that makes it clear what the error is and where it occurred.
 * When you wrap the collect function inside a try-catch block, you are catching any exceptions that might occur inside the collect function.
 * However, this can make it difficult to trace the origin of the exception, as it may have been thrown by a function call inside the collect function. This can lead to confusion and make debugging more difficult.
 */
private fun exceptionTransparencyIssue() = runBlocking {
    try {
        flowOf("a", "b", "c")
            .collect {
                println(it)
                throw RuntimeException("exceptionTransparencyIssue exception!")
            }
    } catch (e: RuntimeException) {
        println("exceptionTransparencyIssue error: $e")
    }
}

/**
 * the [catch] operator is transparent to exceptions that occur in downstream flow and does not catch exceptions that are thrown to cancel the flow.
 */
private fun exceptionTransparencySolution() = runBlocking {
    val flow = flow {
        emit(1)
        throw RuntimeException("exceptionTransparencySolution: exception in emission!")
        emit(2)
    }.catch { e ->
        emit(-1)
    }

    flow.collect { value ->
        throw RuntimeException("exceptionTransparencySolution: exception in Collect!") // catch operator does not catch exceptions in consumer side
        println(value)
    }
}
