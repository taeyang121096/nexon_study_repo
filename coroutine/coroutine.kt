fun main() {
    val start = System.currentTimeMillis()
    val threads = List(100) {
        Thread {
            Thread.sleep(1000) // blocking
        }.apply { start() }
    }
    threads.forEach { it.join() }
    println("Threads elapsed: ${System.currentTimeMillis() - start} ms")
}

fun main() = runBlocking {
    val start = System.currentTimeMillis()
    val jobs = List(100) {
        launch {
            delay(1000) // non-blocking
        }
    }
    jobs.forEach { it.join() }
    println("Coroutines elapsed: ${System.currentTimeMillis() - start} ms")
}
