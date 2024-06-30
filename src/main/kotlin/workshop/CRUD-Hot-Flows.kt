package workshop

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main(): Unit = runBlocking {
    get()
    update()
    update2()
    delete()
}

private fun get(): Unit = runBlocking {
    val hotFlow1 = MutableStateFlow("Hello")
    println("get Hot Flow: ${hotFlow1.value}")
    hotFlow1.collect {
        println("get Hot Flow: $it")
    }
}

private fun update(): Unit = runBlocking {
    val hotFlow2 = MutableStateFlow("1")
    hotFlow2.update { "2" }
    hotFlow2.collect {
        println("Update Hot Flow: $it")
    }
}

private fun update2(): Unit = runBlocking {
    val hotFlow3 = MutableStateFlow(Person.DEFAULT)
    hotFlow3.update { prevState ->
        prevState.copy(name = "Morteza")
    }
    hotFlow3.collect {
        println("Update Hot Flow: $it")
    }
}

private fun delete(): Unit = runBlocking {
    val hotFlow4 = MutableStateFlow(listOf(Person.DEFAULT, Person.MORTEZA))
    hotFlow4.update { prevState ->
        prevState.filterNot { person -> person.name == "Morteza" }
    }
    hotFlow4.collect {
        println("Delete Hot Flow: $it")
    }
}

data class Person(var name: String, var age: Int) {
    companion object {
        val DEFAULT = Person("", 26)
        val MORTEZA = Person("morteza", 26)
    }
}