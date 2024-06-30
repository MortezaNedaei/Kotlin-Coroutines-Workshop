package workshop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun main() {
    flatMapConcat()
    flatMapMerge()
    flatMapLatest()
}

/**
 * [flatMapConcat] operator is used when you want to merge multiple flows into a single flow by sequentially concatenating them one after another.
 * The downstream collector receives the values from the resulting flow in **the same order** they were emitted by the source flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun flatMapConcat() = runBlocking {
    val flow1 = flowOf("A", "B", "C")
    val flow2 = flowOf("D", "E", "F")
    val flow3 = flowOf("G", "H", "I")

    flowOf(flow1, flow2, flow3)
        .flatMapConcat { it }
        .collect {
            println("flatMapConcat $it") // Output: A B C D E F G H I
        }
}

/**
 * [flatMapMerge] operator is used when you want to merge multiple flows into a single flow without any specific order,
 * meaning that the downstream collector may receive values in any order.
 * The resulting flow may emit values from the transformed flows **in any order**.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun flatMapMerge() = runBlocking {
    val flow1 = flowOf("A", "B", "C")
    val flow2 = flowOf("D", "E", "F")
    val flow3 = flowOf("G", "H", "I")

    flowOf(flow1, flow2, flow3)
        .flatMapMerge { it }
        .collect {
            println("flatMapMerge $it")  // Output: A B C D E F G H I
        }
}

/**
 * [flatMapLatest] operator is used when you want to transform the values emitted by a flow to another flow, but you only want to collect values from the most recent transformed flow, and ignore any previously transformed flows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun flatMapLatest() = runBlocking {
    val flow1 = flowOf("A", "B", "C")
    val flow2 = flowOf("D", "E", "F").onEach { delay(100) }
    val flow3 = flowOf("G", "H", "I")

    flowOf(flow1, flow2, flow3)
        .flatMapLatest { it }
        .collect {
            println("flatMapLatest $it") // Output: A, B, C, G, H, I
        }
}