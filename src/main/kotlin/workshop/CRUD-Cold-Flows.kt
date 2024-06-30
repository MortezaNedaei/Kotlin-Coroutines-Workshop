package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main(): Unit = runBlocking {

    get()
    update()
    delete()

    // DO ACTION BEFORE EMITTING TO DOWNSTREAM
//    var coldFlow = flowOf("1", "2", "3")
//    coldFlow = coldFlow.onEach {
//        if (it == "2") {
//            it.plus("_Second")
//        }
//    }

}

private fun get(): Unit = runBlocking {
    val a = flowOf("1", "2", "3")
    a.map { //*** Doesn't work for get
        println("Get: $it")
    }
    a.collect {
        println("Get: $it")
    }
}

private fun update(): Unit = runBlocking {
    var coldFlow = flowOf("1", "2", "3")
    coldFlow = coldFlow.map {
        if (it == "2") it.plus("_Second")
        else it
    }
    coldFlow.collect {
        println("Update: $it")
    }
}

private fun delete(): Unit = runBlocking {
    var coldFlow = flowOf("1", "2", "3")
    coldFlow = coldFlow.filterNot { it == "3" }
    coldFlow.collect {
        println("Delete:: $it")
    }
}