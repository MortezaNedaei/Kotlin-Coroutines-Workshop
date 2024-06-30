package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Combine flows
 * Combine - Loseless in image compression (PNG, TIFF, GIF, etc.).
 * Zip [Danger]! It's like Lossy image compression in image processing (JPEG, MP3, MPEG, etc.). Means some values are removed after zip.
 */
fun main() = runBlocking {
    combineFlows()
    zipFlows()
    combineSharedFlows()
    zipSharedFlows()
}

private fun combineFlows(): Unit = runBlocking {
    val a = flow<String> { // 1, 2, 3
        emit("1")
        delay(1000)
        emit("2")
        delay(1000)
        emit("3")
    }
    val b = flow<String> { // 1, 2, 3, 4, 5
        emitAll(a)
        delay(1000)
        emit("4")
        delay(1000)
        emit("5")
    }
    val combine = a.combine(b) { a, b -> "$a $b" }

    combine.collect {
        println("Combine Flow1: $it")
    }
}

private fun zipFlows(): Unit = runBlocking {
    val a = flow<String> { // 1, 2, 3
        emit("1")
        delay(1000)
        emit("2")
        delay(1000)
        emit("3")
    }
    val b = flow<String> { // 1, 2, 3, 4, 5
        emitAll(a)
        delay(1000)
        emit("4")
        delay(1000)
        emit("5")
    }

    val zip = a.zip(b) { a, b -> "$a $b" }

    zip.collect {
        println("Combine Flow2: $it")
    }
}

private fun combineSharedFlows(): Unit = runBlocking {
    val a = MutableSharedFlow<String>(replay = 2).apply {
        emit("1")
        emit("2")
    }
    val b = MutableSharedFlow<String>(replay = 2).apply {
        emit("3")
        emit("4")
    }
    val combine = combine(a, b) { a, b ->
        "$a $b"
    }

    combine.collect {
        println("Combine SharedFlow: $it")
    }
}

/**
 * the item "4" is zipped and dropped out.
 */
private fun zipSharedFlows(): Unit = runBlocking {
    val a = MutableSharedFlow<String>(replay = 2).apply {
        emit("1")
    }
    val b = MutableSharedFlow<String>(replay = 2).apply {
        emit("3")
        emit("4")
    }
    val zip = a.zip(b) { a, b -> "$a $b" }

    zip.collect {
        println("Zip SharedFlow: $it")
    }
}