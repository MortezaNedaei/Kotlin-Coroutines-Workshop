package workshop.channel

import kotlinx.coroutines.CoroutineScope

object MediatorChannel {
    fun produceAndConsume(coroutineScope: CoroutineScope) {
        ReceiveChannelProducer.produceData(coroutineScope)
        ReceiveChannelConsumer.consumeData(coroutineScope)
    }
}