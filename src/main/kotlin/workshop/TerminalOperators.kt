package workshop

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {
    collect()
    collectLatest()
    launchIn()
    single()
}

private fun collect() = runBlocking {
    flow {
        emit(1)
        delay(50)
        emit(2)
    }.collect { value ->
        println("Collecting $value")
        delay(100) // Emulate work
        println("rest of job $value collected")
    }
}

/**
 * The crucial difference from [collect] is that when the original flow emits a new value then the action block for the previous value is cancelled.
 */
private fun collectLatest() = runBlocking {
    flow {
        emit(1)
        delay(50)
        emit(2)
    }.collectLatest { value ->
        println("Collecting $value") // it prints all items but cancels the previous action block. so rest of job of previous action will be cancelled
        delay(100) // Emulate work
        println("rest of job $value collected")
    }

}

private fun collectVsCollectLatest() = runBlocking {
    flow {
        emit(1)
        delay(50)
        emit(2)
    }.collect { value ->
        println("Collecting $value")
        delay(100) // Emulate work
        println("$value collected")
    }

}

private fun launchIn() = runBlocking {
    flowOf("a", "b", "c", "d")
        .onEach { println("emit items using onEach: $it") }
        .launchIn(this)
}

/**
 * The terminal operator that awaits for one and only one value to be emitted. Returns the single value or null, if the flow was empty or emitted more than one value.
 */
private fun single() = runBlocking {
    flowOf("a", "b", "c", "d")
        .onEach { println("emit items using onEach: $it") } // prints a, b. Why? Because onEach acts before emitting the items from producer
        .single().let {
            println("singleOrNull: $it") // prints null. Because single returns null if there is more than one value
        }
}