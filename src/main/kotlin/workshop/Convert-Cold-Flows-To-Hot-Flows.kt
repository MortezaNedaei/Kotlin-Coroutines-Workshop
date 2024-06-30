package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*

fun main() {
    toSharedFlow()
    toStateFlow()
    toReceiveChannel()
}

private fun toSharedFlow(): Unit = runBlocking {
    println("Converting to SharedFlow ...")
    val coldFlow = flowOf("a", "b", "c", "d")
    val sharedFlow = coldFlow.shareIn(
        scope = this,
        started = SharingStarted.WhileSubscribed() // Sharing is started when the first subscriber appears, immediately stops when the last subscriber disappears (by default), keeping the replay cache forever (by default).
    )
    sharedFlow.collect { println(it) }
}

/**
 * [StateFlow] is a customized type of [SharedFlow] which uses `distinctUntilChanged` internally
 */
private fun toStateFlow(): Unit = runBlocking {
    println("Converted to SharedFlow ...")
    val coldFlow = flowOf("a", "b", "c", "d")
    val stateFlow = coldFlow.stateIn(
        scope = this,
        started = SharingStarted.WhileSubscribed(), // Sharing is started when the first subscriber appears, immediately stops when the last subscriber disappears (by default), keeping the replay cache forever (by default).
        "0"
    )
    stateFlow.collect { println(it) }
}

/**
 * [ReceiveChannel] is a type of channel that provides a way to receive data from a coroutine. It is used when you want to consume data from a channel without being able to send any data back to the sender.
 *
 * By using a ReceiveChannel for the consumer coroutine, we ensure that it can only receive data and cannot accidentally send data back. This separation of concerns helps make the code more maintainable and less error-prone.
 * @see [produceIn] to convert flow to ReceiveChannel: ```val receiveChannel = flowOf("").produceIn(coroutineScope)```
 *
 * There was a [BroadcastChannel] in coroutines which is deprecated due to introduction of [SharedFlow]
 */
private fun toReceiveChannel(): Unit = runBlocking {
    println("Converted to ReceiveChannel ...")
    val coldFlow = flowOf("a", "b", "c", "d")
    val receiveChannel = coldFlow.produceIn(scope = this)
    receiveChannel.consumeEach { println(it) }
}