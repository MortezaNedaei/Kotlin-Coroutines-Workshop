package workshop.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

fun main() = runBlocking {
    pipeline()
}

/**
 * A pipeline is a pattern where one coroutine is producing, possibly infinite, stream of values.
 * And another coroutine or coroutines are consuming that stream, doing some processing, and producing some other results.
 *
 */
private fun pipeline() {
    primeNumbersPipeline()
}

/**
 * The following example prints the first ten prime numbers, running the whole pipeline in the context of the main thread.
 * Since all the coroutines are launched in the scope of the main runBlocking coroutine we don't have to keep an explicit list of all the coroutines we have started.
 * We use cancelChildren extension function to cancel all the children coroutines after we have printed the first ten prime numbers.
 *
 */
fun primeNumbersPipeline() = runBlocking {
    var cur = numbersFrom(2)
    repeat(10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
        delay(1000L)
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}

fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}

fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}