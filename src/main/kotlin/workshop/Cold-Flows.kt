package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * In Cold Flows, the values are only computed when requested by collector.
 */
fun main() {
    flowBlockNeedsATerminalOperator()
    nonInfiniteFlow()
    infiniteFlow()
    contextSwitching()
}

/**
 * The flow {} builder block is not executed until invoke a terminal operator like `collect{}`
 */
fun flowBlockNeedsATerminalOperator() = runBlocking {
    run {
        println("Hello")
    }

    flow<String> { // This block is not executed until invoke a terminal operator like `collect{}`
        println("Hello Flow")
    }
}

/**
 * Non-Infinite Flow
 * It doesn't emit anything. Since there is no requests from any collector
 */
fun nonInfiniteFlow() = flow {
    while (true) {
        emit("non-infinite flow!")
    }
}

/**
 * Infinite Flow
 * There is a collector that requests to collect values from producer. So the flow emits the values.
 * It's an infinite flow, since there is while true and also a collector
 */
fun infiniteFlow() = runBlocking {
    val infiniteFlow = flow {
        while (true) {
            emit("infinite flow!")
        }
    }
    infiniteFlow.collect {
        println(it)
    }
}

fun contextSwitching() = runBlocking {
        val flow = flowOf("a", "b", "c", "d")
            .onStart { println("Flow onStart1") }
            .flowOn(Dispatchers.Default)
            .onEach { println("Flow onEach: $it")  }
            .flowOn(Dispatchers.IO)
            .onStart { println("Flow onStart2")  }

    launch(Dispatchers.Default) {
        flow.collect { println("Collector: $it") }
    }
}
