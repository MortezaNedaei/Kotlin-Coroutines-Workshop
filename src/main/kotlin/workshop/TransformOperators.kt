package workshop

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {
    transform()
    transform2()
    map()
    onEach()
    onEach2()
    filterNotNull()
    filter()

    fold()
    runningFold()
    reduce()
    runningReduce()
    scan()
}

private fun transform() = runBlocking {
    flowOf("a", "bb", "c", "dd")
        .transform { value ->
            if (value.length > 1) emit(value)
        }
        .collect { println("emit items with transfer: $it") }
}

private fun transform2() = runBlocking {
    flowOf(1, 2, 3, 4, 5)
        .transform { value ->
            emit("Value: $value")
        }.collect { println(it) }
}


/**
 * `onEach` IS NOT A TERMINAL OPERATOR
 * Why `onEach` does not print anything???
 * Because onEach is not a terminal operator. in cold flows, a terminal operator like `collect` or launchIn() needs to request producer to emit data
 * `onEach` is invoked before emitting values by producer.So it's usually in producer side.
 * It's also be invoked before `onStart`.
 * onEach is used to do something when each value is emitted by producer. (NOT CHANGING OR TRANSFORMING THE REAL FLOW).
 * To transform current flow, use [map] instead. See [TerminalOperators.kt]
 */
private fun onEach() = runBlocking {
    flowOf("a", "b", "c", "d")
        .onEach { println("Emitting: $it") } // does not print anything. Since a terminal operator like collect needs to request producer to emit data
}

/**
 * `onEach` is invoked before emitting values by producer.So it's usually in producer side.
 * It's also be invoked before `onStart`.
 * onEach is used to do something when each value is emitted by producer. (NOT CHANGING OR TRANSFORMING THE REAL FLOW).
 * To transform current flow, use [map] instead. See [TerminalOperators.kt]
 *
 * Similar to:
 * ```
 * flow {
 *   emit("a")
 *   delay(1000)
 *   emit("b")
 *   delay(1000)
 * }.collect {}
 * ```
 */
private fun onEach2() = runBlocking {
    flowOf("a", "b", "c", "d")
        .onEach { delay(1000) }
        .collect { println("emit items with delay: $it") }
}

private fun map() = runBlocking {
    flowOf("a", "b", "c", "d")
        .onEach { delay(500) }
        .map { "Hello $it" }
        .collect { println("emit items with delay: $it") }
}


private fun filterNotNull() = runBlocking {
    flowOf("a", null, "b", "c", null, "d")
        .filterNotNull()
        .collect { println("emit items with null filter: $it") }
}

private fun filter() = runBlocking {
    flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        .filter { it % 2 == 0 }
        .collect { println("emit items with null filter: $it") }
}

/**
 * Accumulates value starting with initial value and applying operation current accumulator value and each element
 * [fold] is a terminal operator as well as the transform operator, according to https://kotlinlang.org/docs/flow.html#terminal-flow-operators
 * @see scan as well
 */
private fun fold() = runBlocking {
    flowOf(2, 5, 7, 10)
        .fold(0) { acc, i -> acc + i }
        .also { println("fold: $it") }
}

/**
 * Similar to [fold]. but returns a flow of values instead of final value
 */
private fun runningFold() = runBlocking {
    flowOf(2, 5, 7, 10)
        .runningFold(0) { acc, i -> acc + i } // or reduce to get the final result instead of flow
        .collect { println("runningReduce: $it") }
}

/**
 * Similar to [fold] but starts with first element of flow
 *  [reduce] is a terminal operator as well as the transform operator, according to https://kotlinlang.org/docs/flow.html#terminal-flow-operators
 */
private fun reduce() = runBlocking {
    flowOf(2, 5, 7, 10)
        .reduce { acc, i -> acc + i }
        .also { println("reduce: $it") }
}

/**
 * Similar to [reduce]. but returns a flow of values instead of final value
 */
private fun runningReduce() = runBlocking {
    flowOf(2, 5, 7, 10)
        .runningReduce { acc, i -> acc + i } // or reduce to get the final result instead of flow
        .collect { println("runningReduce: $it") }
}

/**
 * Exactly Similar to [runningFold]. It only has a better function name
 */
private fun scan() = runBlocking {
    flowOf(2, 5, 7, 10)
        .scan(0) { acc, i -> acc + i }
        .collect { println("scan: $it") }
}