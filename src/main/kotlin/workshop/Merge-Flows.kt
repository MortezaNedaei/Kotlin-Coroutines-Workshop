package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Merge flows
 * merge
 */
fun main() = runBlocking {
    mergeColdFlows()
    mergeSharedFlows()
}

fun mergeColdFlows() = runBlocking {
    val flow1 = flow<String> { // 1, 2, 3
        emit("1")
        delay(1000)
        emit("2")
        delay(1000)
        emit("3")
    }
    val flow2 = flow<String> { // 1, 2, 3, 4, 5
        emitAll(flowOf("morteza", "morteza2", "morteza3"))
        delay(1000)
        emit("morteza4")
        delay(1000)
        emit("morteza5")
    }

    val merge = merge(flow1, flow2)

    merge.collect {
        println("Combine Flow3: $it")
    }
}

/**
 * Merges all items from several flows even if they're emitted from different coroutine contexts safely
 */
private fun mergeSharedFlows(): Unit = runBlocking {
    val flow1 = MutableSharedFlow<String>(replay = 2).apply {
        launch { emit("1") }
    }
    val flow2 = MutableSharedFlow<String>(replay = 2).apply {
        launch {
            emit("3")
            emit("4")
        }
    }
    val merge = merge(flow1, flow2)

    merge.collect {
        println("Merge SharedFlow: $it")
    }
}