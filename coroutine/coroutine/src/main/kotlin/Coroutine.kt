import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

// -------------------------------------
// I/O 시뮬레이션 함수
// -------------------------------------
fun fetchDataA_IO(): String {
    Thread.sleep(1000) // 1초 I/O 지연
    return "Data A from I/O"
}

fun fetchDataB_IO(): String {
    Thread.sleep(1000) // 1초 I/O 지연
    return "Data B from I/O"
}

// -------------------------------------
// 1️⃣ 동기 I/O
// -------------------------------------
fun syncIOExample() {
    println("=== 동기 I/O 실행 ===")
    val start = System.currentTimeMillis()

    val a = fetchDataA_IO()
    val b = fetchDataB_IO()

    println("Result: $a & $b")
    println("Time taken: ${(System.currentTimeMillis() - start) / 1000.0} sec\n")
}

// -------------------------------------
// 2️⃣ 스레드 기반 비동기 I/O
// -------------------------------------
fun threadAsyncIOExample() {
    println("=== 스레드 기반 비동기 I/O ===")
    val start = System.currentTimeMillis()

    val executor = Executors.newFixedThreadPool(2)
    val futureA: Future<String> = executor.submit(Callable { fetchDataA_IO() })
    val futureB: Future<String> = executor.submit(Callable { fetchDataB_IO() })

    val a = futureA.get()
    val b = futureB.get()

    println("Result: $a & $b")
    println("Time taken: ${(System.currentTimeMillis() - start) / 1000.0} sec\n")

    executor.shutdown()
}

// -------------------------------------
// 3️⃣ 코루틴 기반 비동기 I/O
// -------------------------------------

fun coroutineIOExample() = runBlocking {
    println("=== 코루틴 100,000번 병렬 실행 시간 측정 ===")
    val start = System.currentTimeMillis()
    val jobs = List(100000) { async { delay(1000) } }
    jobs.awaitAll()
    println("Time taken: ${(System.currentTimeMillis() - start) / 1000.0} sec\n")
}

// -------------------------------------
// Main
// -------------------------------------
fun main() {
    syncIOExample()          // 약 2000ms 예상
    threadAsyncIOExample()   // 약 1000ms 예상
    coroutineIOExample()     // 약 1000ms 예상
}
