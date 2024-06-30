package workshop

import workshop.channel.MediatorChannel
import workshop.channel.primeNumbersPipeline
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.produceIn

fun main() {
    runBlocking {
        channel(this)
        rendezvousChannel()
        receiveChannel(this)
        encapsulatedReceiveChannel(this)
        pipeline()
        fanOut()
        fanIn()
        tickerChannel()
    }
}

/**
 * Channel enables communication between two or more coroutines.
 * One or multiple coroutines can send information to the same channel, and one or multiple coroutines can receive data from it
 */
fun channel(coroutineScope: CoroutineScope) {
    val channel = Channel<String>()

    // Producer starts sending data inside different coroutines
    coroutineScope.launch {
        println("Channel: Coroutine 1 Send to channel")
        channel.send("1")
    }
    coroutineScope.launch {
        println("Channel: Coroutine 2 Send to channel")
        channel.send("2")
        channel.close() // we're done sending so channel should be closed
    }

    // Consumer starts receiving data inside another coroutines
    coroutineScope.launch {
        channel.receive().also {
            channel.send("3")
            println("Channel: Coroutine 1 Received: $it")
        }
        println("Channel: Done!")
    }
    coroutineScope.launch {
        channel.consumeEach {
            println("Channel: Coroutine 2 Received: $it")
        }
        println("Channel: Done!") // This line called when channel is closed
    }
    coroutineScope.launch {
        delay(2000)
        channel.send("4") // throws exception. Use ReceiveChannel to prevent sending data by consumer accidentally. Or use [trySend]
        channel.tryReceive().also { // avoid throwing exception when the channel is closed
            println("Channel: Coroutine 3 Received: $it")
        }
    }
}

/**
 * default of channels, has no buffer, uses suspending function to transmit data only when producer and consumer meet each other.
 */
fun rendezvousChannel() = runBlocking {

    val channel = Channel<String>()

    launch { // coroutine1
        val fruits = listOf("Apple", "Orange", "Banana", "Grape", "Mango")
        for (fruit in fruits) {
            println("coroutine1: Sending $fruit")
            channel.send(fruit)
        }
    }

    launch { // coroutine2
        repeat(5) {
            delay(5000)
            println("coroutine2: Received ${channel.receive()}")
        }
    }
}

/**
 * [ReceiveChannel] is a type of channel that provides a way to receive data from a coroutine. It is used when you want to consume data from a channel without being able to send any data back to the sender.
 *
 * By using a ReceiveChannel for the consumer coroutine, we ensure that it can only receive data and cannot accidentally send data back. This separation of concerns helps make the code more maintainable and less error-prone.
 * @see [produceIn] to convert flow to ReceiveChannel: ```val receiveChannel = flowOf("").produceIn(coroutineScope)```
 *
 * There was a [BroadcastChannel] in coroutines which is deprecated due to introduction of [SharedFlow]
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun receiveChannel(coroutineScope: CoroutineScope) {

    var channel: ReceiveChannel<String> = Channel()

    // Producer Coroutine
    coroutineScope.launch {
        channel = produce {
            println("This is a Hot stream. Because this block is executed even if you don't call consumeEach.")
            send("A")
            send("B")
            send("C")
            send("D")
            // we don't have to close the channel explicitly
        }
    }

    // Consumer Coroutine
    coroutineScope.launch {
        channel.consumeEach {
            println("ReceiveChannel: Received $it")
        }
        // sending back data to channel inside consumer coroutine is not possible
        // because it is a ReceiveChannel
        // channel.send("E")

        // channel is automatically closed
        println("ReceiveChannel: Is producer closed: ${channel.isClosedForReceive}")
    }
}

fun encapsulatedReceiveChannel(coroutineScope: CoroutineScope) {
    MediatorChannel.produceAndConsume(coroutineScope)
}

/**
 * A pipeline is a pattern where one coroutine is producing, possibly infinite, stream of values.
 * And another coroutine or coroutines are consuming that stream, doing some processing, and producing some other results.
 */
private fun pipeline() {
    primeNumbersPipeline()
}

/**
 * Multiple coroutines may receive from the same channel
 */
private fun fanOut() = runBlocking {
    val channel = produce {
        send("1")
        send("2")
    }
    repeat(2) {
        launch { println(channel.receive()) }
    }
}

/**
 * Multiple coroutines may send to the same channel.
 */
private fun fanIn() = runBlocking {
    val channel = Channel<String>()

    // Producer starts sending data inside different coroutines
    launch { channel.send("1") }
    launch { channel.send("2") }
    repeat(2) {
        println(channel.receive())
    }
}

/**
 * Ticker channel is a special rendezvous channel that produces Unit every time given delay passes since last consumption from this channel.
 * Though it may seem to be useless standalone, it is a useful building block to create complex time-based produce pipelines and operators that do windowing and other time-dependent processing.
 * Ticker channel can be used in select to perform "on tick" action.
 *
 */
private fun tickerChannel() = runBlocking {
    val tickerChannel = ticker(delayMillis = 200, initialDelayMillis = 0) // create a ticker channel
    var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Initial element is available immediately: $nextElement") // no initial delay

    nextElement = withTimeoutOrNull(100) { tickerChannel.receive() } // all subsequent elements have 200ms delay
    println("Next element is not ready in 100 ms: $nextElement")

    nextElement = withTimeoutOrNull(120) { tickerChannel.receive() }
    println("Next element is ready in 200 ms: $nextElement")

    // Emulate large consumption delays
    println("Consumer pauses for 300ms")
    delay(300)
    // Next element is available immediately
    nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Next element is available immediately after large consumer delay: $nextElement")
    // Note that the pause between `receive` calls is taken into account and next element arrives faster
    nextElement = withTimeoutOrNull(120) { tickerChannel.receive() }
    println("Next element is ready in 100ms after consumer pause in 300ms: $nextElement")

    tickerChannel.cancel() // indicate that no more elements are needed
}

