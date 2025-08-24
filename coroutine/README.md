# 코루틴(Coroutines) 스터디

## 1. 코루틴의 개념
비동기 프로그래밍은 I/O나 네트워크 요청 처리에서 자원 효율성과 사용자 경험을 높이기 위해 필수적이다.  
코루틴(Coroutine)은 이 같은 비동기 작업을 간편하게 구현할 수 있는 프로그래밍 모델이다.  

- **정의**: 실행 도중 특정 시점에 중단(suspend)했다가 나중에 재개(resume)할 수 있는 함수.
- **장점**
  - 콜백 지옥(callback hell) 방지
  - 스레드보다 가볍고 수천 개 동시 실행 가능
  - 동기식 코드 스타일로 비동기 흐름 표현
- **언어 지원**
  - Java: 기본 코루틴 문법 없음. `Thread`, `CompletableFuture`, Java 21부터 Virtual Threads
  - Kotlin: 언어 차원의 `suspend`/코루틴 지원
  - JavaScript: `Promise`, `async/await`
  - Python: `asyncio`, `async/await`

---

## 2. Java: 전통적인 비동기 처리
자바는 기본적으로 스레드 기반 동시성을 사용한다. Java 8부터 `CompletableFuture`로 비동기를 편리하게 처리할 수 있으며, Java 21부터 **Virtual Threads**가 도입되어 경량 스레드 모델을 제공한다.

### 비동기 HTTP 요청 예제
```java
import java.net.http.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class HttpExample {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.example.com/data"))
            .build();

        CompletableFuture<HttpResponse<String>> responseFuture =
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        responseFuture.thenApply(HttpResponse::body)
                      .thenAccept(System.out::println);

        responseFuture.join(); // 결과 대기
    }
}
```

### 백엔드 작업 예제
```java
import java.util.concurrent.*;

public class BackendExample {
    public static void main(String[] args) {
        CompletableFuture<Integer> sumFuture = CompletableFuture.supplyAsync(() -> {
            int sum = 0;
            for (int i = 1; i <= 1_000_000; i++) sum += i;
            return sum;
        });

        sumFuture.thenAccept(sum -> System.out.println("Sum: " + sum));
        sumFuture.join();
    }
}
```

---

## 3. Kotlin: 언어 수준의 코루틴 지원
Kotlin은 `suspend` 함수와 `CoroutineScope`를 통해 언어 차원에서 코루틴을 지원한다.  
**구조적 동시성**을 지원하여 스코프가 끝나면 관련 코루틴이 자동 취소된다.

### 비동기 HTTP 요청 예제
```kotlin
import kotlinx.coroutines.*
import java.net.URL

fun main() = runBlocking {
    val deferred = async(Dispatchers.IO) {
        URL("https://api.example.com/data").readText()
    }
    println("Response: ${deferred.await()}")
}
```

### 백엔드 작업 예제
```kotlin
import kotlinx.coroutines.*

suspend fun computeSum(n: Int): Int = withContext(Dispatchers.Default) {
    (1..n).sum()
}

fun main() = runBlocking {
    val sum = computeSum(1_000_000)
    println("Sum: $sum")
}
```

---

## 4. JavaScript: 이벤트 루프 기반 비동기
JavaScript는 단일 스레드 언어지만 **이벤트 루프**와 `Promise`, `async/await`를 통해 비동기를 처리한다.

### 비동기 HTTP 요청 예제
```javascript
async function fetchData() {
    console.log("Fetch 시작");
    const response = await fetch("https://api.example.com/data");
    const data = await response.json();
    console.log(data);
}
fetchData();
```

### 백엔드 작업 예제 (Node.js)
```javascript
const fs = require('fs').promises;

async function readFileAsync() {
    try {
        const data = await fs.readFile('example.txt', 'utf-8');
        console.log(data);
    } catch (err) {
        console.error(err);
    }
}
readFileAsync();
```

---

## 5. Python: `asyncio` 기반 코루틴
Python은 `async def`와 `await`를 통해 코루틴을 정의하며, `asyncio` 이벤트 루프에서 실행한다.

### 비동기 HTTP 요청 예제
```python
import asyncio
import aiohttp

async def fetch_data():
    async with aiohttp.ClientSession() as session:
        async with session.get("https://api.example.com/data") as response:
            return await response.text()

async def main():
    print("Fetching...")
    data = await fetch_data()
    print("Data:", data)

asyncio.run(main())
```

### 백엔드 작업 예제
```python
import asyncio
import time

async def say_after(delay, message):
    await asyncio.sleep(delay)
    print(message)

async def main():
    task1 = asyncio.create_task(say_after(1, "Hello"))
    task2 = asyncio.create_task(say_after(2, "World"))
    print("started at", time.strftime("%X"))
    await task1
    await task2
    print("finished at", time.strftime("%X"))

asyncio.run(main())
```

---

## 6. Java와 Kotlin의 차이점
| 구분 | Java | Kotlin |
|------|------|---------|
| **언어 지원** | 코루틴 문법 없음. `Thread`, `CompletableFuture`, Java 21 Virtual Threads 사용 | 언어 차원 코루틴 지원 (`suspend`, `launch`, `async`) |
| **비동기 처리** | 콜백, Future 기반 | 구조적 동시성, 코루틴 빌더 |
| **성능/편의성** | 스레드 무거움. Virtual Thread로 개선 | 경량 코루틴. 수천 개 동시 실행 가능 |
| **활용 예시** | 서버 백엔드(CompletableFuture, Loom) | 안드로이드, 서버, 멀티플랫폼 전반 |

---

## 참고 자료
- [Kotlin Coroutines 공식 문서](https://kotlinlang.org/docs/coroutines-overview.html)  
- [Python asyncio 공식 문서](https://docs.python.org/3/library/asyncio.html)  
- [MDN JavaScript async/await 문서](https://developer.mozilla.org/ko/docs/Learn/JavaScript/Asynchronous/Promises)  
- [Java 21 Virtual Threads (Project Loom)](https://openjdk.org/jeps/444)  
