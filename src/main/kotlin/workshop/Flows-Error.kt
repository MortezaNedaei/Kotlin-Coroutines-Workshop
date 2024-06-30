package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main(): Unit = runBlocking {

    flow {
        emit("0")
        throw RuntimeException("An exception occurred")
        emit("1")
    }
        .catch { ex ->
            println("catch: $ex")
            emit("Emit alternative value: 1")
        }
        .onStart { println("onStart") }
        .onCompletion { println("onCompletion") }
        .collect { println(it) }
}