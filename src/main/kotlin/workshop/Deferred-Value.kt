package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Deferred value is a non-blocking cancellable future — it is a Job with a result.
 * It is created with the async coroutine builder or via the constructor of CompletableDeferred class. It is in active state while the value is being computed.
 */
fun main(): Unit = runBlocking {
    deferred1()
    deferred2()
    deferred3()
}


/**
 * `async { ... }` starts computation in background immediately to produce result later.
 */
fun deferred1() = runBlocking {
    val deferred = async { getData() }
    val flow: Flow<String> = deferred.await()

    flow.collect { println(it) }
}

/**
 * Similar to [deferred1]
 */
fun deferred2() = runBlocking {
    val deferred2 = CompletableDeferred<Flow<String>>()
    deferred2.complete(getData())
    val flow2 = deferred2.await()

    flow2.collect { println(it) }
}

/**
 * `async(start = CoroutineStart.LAZY) { ... }` starts computation only when it is requested, sharing result with subsequent requests.
 */
fun deferred3() = runBlocking {
    val deferred = async(start = CoroutineStart.LAZY) { getData() }
    val flow: Flow<String> = deferred.await()

    flow.collect { println(it) }
}

private suspend fun getData() = flow {
    emit("Hello")
    emit("World !")
}