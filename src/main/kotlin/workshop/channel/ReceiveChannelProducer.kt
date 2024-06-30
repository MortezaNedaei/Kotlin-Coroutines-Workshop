package workshop.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

object ReceiveChannelProducer {

    /**
     * [channel] can ony be updated by this class. So the consumer class can't send new data
     */
    var channel: ReceiveChannel<String> = Channel()
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    fun produceData(scope: CoroutineScope) = scope.produce {
        send("1")
        send("2")
    }.also {
        channel = it // channel is closed here automatically
    }
}