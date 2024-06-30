package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*

/**
 * SharedFlows keeps the **most recent values** among **multiple consumers** which uses a **lock** for thread safety.
 *
 * The complexity of emit function in SharedFlow is O(N) - N: size of consumers
 *
 * StateFlow is a SharedFlow that uses [distinctUntilChanged] internally. means all repetitions of the same value are filtered out.
 *
 * StateFlow also uses replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST as parameters
 */
fun main(): Unit = runBlocking {
    sharedFlow()
    sharedFlowReplayCache()
    sharedFlowSharesSameValuesWithAllConsumers()
    sharedFlowNeverCompletes()
    stateFlow()
    createStateFlowFromSharedFlow()
    hotFlowsDontNeedTerminalOperator()
    eventBus()
    rendezvousEventBus()
}

/**
 * SharedFlows is a type of Flows that keeps the **most recent values** among **multiple consumers** which uses a **lock** for thread safety.
 *  To increase number of most recent of values, you need to change the replay cache parameter
 *  E.g: replay=3 means that the shared flow keeps the most recent 3 values in its replay cache.
 *
 * [SharedFlow] is like an event bus and can emit the same value multiple times.
 *
 * **Shared flow never completes**. A call to Flow. collect on a shared flow never completes normally
 *
 * replay: A shared flow keeps a specific number of the most recent values in its replay cache.
 * Every new subscriber first gets the values from the replay cache and then gets new emitted values.
 *
 * NOTE1: The SharedFlow consumer doesn't collect each item exactly when it's emitted from upstream. It waits for all elements to be emitted. Then consumer collect the most recent values in the cache. To simulate this behavior, use [delay] during emitting the values.
 *
 * NOTES2: for replay > 1, when one of the consumers gets a most recent value, the next consumers won't receive that value. Instead, they receive next recent values. (Similar to [Channels]).
 *
 * What is **Operator Fusion**: Usage of `flowOn`, `buffer` with `RENDEZVOUS` capacity, or cancellable operators to a shared flow **has no effect**.
 *
 * a type of Flow interface
 * Hot
 * َUsage: Hold most recent values among Multiple consumer using Replay cache
 * (zero or more consumer) -> O(N)
 * uses O(1) for adding new consumer
 * Uses lock for thread safety of all consumers
 * Event Bus
 * Never completes (onCompletion never invoked until take or takeWhile that cancels coroutine’s job)
 * Uses Replay cache to add more recent values
 * Overflow and capacity strategies
 *
 */
private fun sharedFlow(): Unit = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    sharedFlow.emit(1) // emit the initial value

    sharedFlow.collect {
        println("SharedFlow: $it")
        delay(1000)
        sharedFlow.emit(1)
    }
}

/**
 * SharedFlows keeps the most recent values. To increase number of most recent of values, you need to change the replay cache parameter
 * E.g: replay=3 means that the shared flow keeps the most recent 3 values in its replay cache.
 *
 * NOTE1: The SharedFlow consumer doesn't collect each item exactly when it's emitted from upstream. It waits for all elements to be emitted. Then consumer collect the most recent values in the cache. To simulate this behavior, use [delay] during emitting the values.
 *
 * What is **Operator Fusion**: Usage of `flowOn`, `buffer` with `RENDEZVOUS` capacity, or cancellable operators to a shared flow **has no effect**.
 */
private fun sharedFlowReplayCache(): Unit = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>(replay = 3, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    sharedFlow.emit(1) // emit the initial value
    delay(1000)
    sharedFlow.emit(2)
    delay(1000)
    sharedFlow.emit(3)
    delay(1000)
    sharedFlow.emit(4)
    delay(1000)

    sharedFlow.collect {
        println("SharedFlow: $it")
//        delay(1000)
    }
}

/**
 * Unlike the [Channel]s, [SharedFlow] shares same values among all consumers
 */
private fun sharedFlowSharesSameValuesWithAllConsumers(): Unit = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    repeat(10) {
        sharedFlow.emit(it)
    }

    // Consumer 1
    launch {
        sharedFlow.collect { println("SharedFlow Consumer1: $it") }
    }

    // Consumer 2
    launch {
        sharedFlow.collect { println("SharedFlow Consumer2: $it") }
    }

    // Consumer 3
    launch {
        sharedFlow.collect { println("SharedFlow Consumer3: $it") }
    }
}

/**
 * [SharedFlow] never completes. Because the implementation of collect in SharedFlows uses a while loop internally. Means the code after collect won't be called.
 * To fix this code, you need to move each collector to a separated coroutine.
 * But this code works for cold flows. Means the cold flows complete unlikely.
 */
private fun sharedFlowNeverCompletes(): Unit = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    sharedFlow.emit(1)

    sharedFlow.collect { println("SharedFlow: $it") }
    sharedFlow.collect { println("SharedFlow: $it") }

    // FIX:
/*  launch { sharedFlow.collect {} }
    launch { sharedFlow.collect {} }  */
}

/**
 * [StateFlow] is a specified [SharedFlow] optimized for sharing state or state machine
 *
 * The main difference between [StateFlow] and [SharedFlow] is that [StateFlow] has `value` property to get the current value synchronously.
 *
 * [StateFlow] uses `distinctUntilChanged` internally.
 *
 */
private fun stateFlow(): Unit = runBlocking {
    val stateFlow = MutableStateFlow(1)
    stateFlow.collect {
        println("Collected $it")
        delay(5000)
        stateFlow.update { 1 } // StateFlow uses `distinctUntilChanged` internally. means all repetitions of the same value are filtered out.
        // stateFlow.update { 2 }
    }
}

/**
 * Hot streams send data regardless of an active a collector, Because the produce block is executed even if you don't call `consumeEach`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun hotFlowsDontNeedTerminalOperator() = runBlocking {
        produce { // ProducerScope
            println("This is a Hot stream. Because this block is executed even if you don't call consumeEach.")
            send("A")
            send("B")
            send("C")
            send("D")
        }
}

/**
 * How to create [Stateflow] using [SharedFlow]
 */
fun createStateFlowFromSharedFlow() = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    sharedFlow.tryEmit(1) // emit the initial value
    val stateFlow = sharedFlow.distinctUntilChanged() // StateFlow-like behavior

    stateFlow.collect {
        println("Create StateFlow using SharedFlow: $it")
        delay(5000)
        sharedFlow.tryEmit(1) // StateFlow uses `distinctUntilChanged` internally. means all repetitions of the same value are filtered out.
    }
}

/**
 * [EventBus]
 *
 * If you use default SharedFlow (replay = 0, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.SUSPEND), Means the SharedFlow is unbuffered and [RENDEZVOUS] like Channels.
 * if you remove all delays, [Conflation] will be happened. Means only the last emitted valued will be collected
 * [Conflation]: consumer is slower than producer. So it won’t receive intermediate emissions and only gets final changes.
 */
fun eventBus(): Unit = runBlocking {
    launch {
        (1..5).forEach {
            EventBus.produceEvent(it.toString())
        }
        // if you remove all delays, Conflation will be happened. Means only the last emitted valued will be collected
        delay(2000L)
        (5..10).forEach {
            EventBus.produceEvent(it.toString())
        }
        delay(2000L)
        (10..15).forEach {
            EventBus.produceEvent(it.toString())
        }
    }

    launch {
        EventBus.events.collect {
            println("EventBus consumer: $it")
        }
    }
}


object EventBus {
    private val _events = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events = _events.asSharedFlow()
//    val events: SharedFlow<String>
//    field: MutableSharedFlow<String> = MutableSharedFlow<String>()

    suspend fun produceEvent(event: String) {
        _events.emit(event) // suspends until all subscribers receive it
    }
}

/**
 * same as [eventBus], but rendezvous
 */
fun rendezvousEventBus(): Unit = runBlocking {
    launch {
        (1..5).forEach {
            delay(1000L)
            RendezvousEventBus.produceEvent(it.toString())
        }
        // if you remove all delays, Conflation will be happened. Means only the last emitted valued will be collected
        (5..10).forEach {
            delay(1000L)
            RendezvousEventBus.produceEvent(it.toString())
        }
        (10..15).forEach {
            delay(1000L)
            RendezvousEventBus.produceEvent(it.toString())
        }
    }

    launch {
        RendezvousEventBus.events.collect {
            println("EventBus consumer: $it")
        }
    }
}

object RendezvousEventBus {
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    suspend fun produceEvent(event: String) {
        _events.emit(event) // suspends until all subscribers receive it
    }
}

