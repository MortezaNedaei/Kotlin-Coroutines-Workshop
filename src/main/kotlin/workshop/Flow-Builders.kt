package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

fun main(): Unit = runBlocking {

    emptyFlow<String>()
    flowOf("")
    flow { emit("") }

    iterator<String> {  }.asFlow()
    arrayOf<String>().asFlow()
    listOf<String>().asFlow()


    /**
     * [callbackFlow] and [channelFlow] are similar. But callbackFlow throws exception if you don't call awaitClose().
     */
    channelFlow {
        trySend("")
        awaitClose { cancel() } // doesn't throw exception if you don't call. So it's not safe to use
    }
    callbackFlow {
        trySend("")
        awaitClose { cancel() } // throws exception if you don't call. So it's safer to use
    }

    MutableSharedFlow<String>() // shares values among all consumers
    MutableStateFlow("") // initialize value is required. Because it's like a state-machine and it will return the most recent value to all consumers

    Channel<String>() // Rendezvous Channel
}