package workshop.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

object ReceiveChannelConsumer {

    fun consumeData(scope: CoroutineScope) = scope.launch {

        // consumer can send data and change the upstream
        // ReceiveChannelProducer.channel = Channel()
        ReceiveChannelProducer.channel.consumeEach {
            println("ReceiveChannelConsumer: Received $it")
        }
    }
}
