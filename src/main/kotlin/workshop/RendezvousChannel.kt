package workshop

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() {

    val channel = Channel<String>()
    
    runBlocking {
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
}