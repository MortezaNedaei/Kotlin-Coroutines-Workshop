package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume

fun main() = runBlocking {
    getSingleValue().let {
        println("continuation: $it")
    }
    getMultipleValuesCallbackFlow().collect {
        println("callbackFlow: $it")
    }
    getMultipleValuesChannelFlow().collect {
        println("channelFlow: $it")
    }
}

suspend fun getSingleValue(): String = suspendCancellableCoroutine { continuation ->
    runBlocking {
        continuation.resume("Morteza") // Single Shot value
        delay(3000)
        continuation.resume("Morteza2") // !! Exception. Shouldn't resume multiple times
    }
}

/**
 * [callbackFlow] is a cold flow which uses [Channel] internally. but returns a [Flow<T>]
 *
 * **unlike [flow], the [callbackFlow] allows values to be emitted from a different [CoroutineContext] with the [send] function or outside a coroutine with the [trySend] function**
 *
 * It is useful when you get your data from a callback based API and want to update your flow based on this callback states.
 *
 * It supports sending or collecting data in different contexts or concurrently
 *
 * [send] vs [trySend]: Use send inside a coroutine. Use trySend outside a coroutine
 */
suspend fun getMultipleValuesCallbackFlow(): Flow<String> = callbackFlow {
    object : CallbackApi<String> {
        override fun onNextValue(value: String) {
            // or `trySendBlocking`
            trySend("callback $value") // outside a coroutine
        }
        override fun onError(error: Throwable) {
            cancel(CancellationException("API Error", error))
        }

        override fun onCompleted() {
            channel.close()
        }
    }.apply {
        (1..3).forEach { onNextValue("$it") }
    }

    send("1")
    delay(3000)
    send("2")
    awaitClose { cancel() }
    /**  awaitClose Suspends the current coroutine until the channel is either [closed][SendChannel.close] or [cancelled][ReceiveChannel.cancel] * and invokes the given [block] before resuming the coroutine.**/
}//.buffer(Channel.UNLIMITED) // To avoid blocking

/**
 * Similar to [callbackFlow]
 *
 * Unlike the [callbackFlow] It is used when you have control over your data
 */
suspend fun getMultipleValuesChannelFlow(): Flow<String> = channelFlow {
    launch(Dispatchers.IO) {
        send("1")
    }
    launch(Dispatchers.Default) {
        send("2")
    }
    awaitClose { cancel() }
}


interface CallbackApi<in T> {
    fun onNextValue(value: T)
    fun onError(error: Throwable)
    fun onCompleted()
}