# 코루틴(Coroutines)

## 0. 들어가기 전에: 비동기 / 동시성 / 병렬성

먼저 용어를 확실히 구분해보자. 많은 사람들이 헷갈려하는 부분이기도 하다.

- **비동기 (Asynchronous)**  
  → "결과가 나올 때까지 기다리지 않고 다른 일을 할 수 있음"  
  예: 파일 다운로드 시작 → 다운로드 끝날 때까지 다른 작업 수행 가능

- **동시성 (Concurrency)**  
  → 여러 작업이 **겹쳐 보이도록** 실행되는 것 (실제로 동시에 실행되지 않아도 됨)  
  예: 하나의 CPU가 여러 작업을 번갈아 빠르게 실행 → 사람 눈에는 동시에 하는 것처럼 보임

- **병렬성 (Parallelism)**  
  → 실제로 **여러 CPU 코어에서 동시에** 실행되는 것  
  예: 4코어 CPU가 각각 다른 연산을 수행

👉 **코루틴은 주로 동시성을 구현**하는 기술이지만, 여러 스레드에 분산되면 병렬성까지 달성할 수 있다.

---

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

## 2. 코루틴과 스레드 비교

| 항목         | 스레드(Thread)                          | 코루틴(Coroutine)               |
|------------|------------------------------------|-----------------------------|
| 관리 주체      | OS (운영체제)                           | 언어 런타임/라이브러리               |
| 생성 비용      | 무겁다 (MB 단위 스택 메모리)                 | 매우 가볍다 (KB 단위)              |
| 개수 제한      | 수천 개 이상 힘듦                          | 수십만 개 가능                   |
| 스케줄링 방식    | 선점형 (OS가 강제로 중단 후 다른 스레드 실행)    | 협력형 (suspend 지점에서만 양보)     |
| 주요 용도      | CPU 집약적 병렬 처리                       | I/O 대기, 동시성 처리, 대규모 요청 처리 |

---

## 3. Java: 전통적인 비동기 처리
자바는 기본적으로 스레드 기반 동시성을 사용한다. Java 8부터 `CompletableFuture`로 비동기를 편리하게 처리할 수 있으며, Java 21부터 **Virtual Threads**가 도입되어 경량 스레드 모델을 제공한다.

### 스레드 직접 사용
```java
public class Main {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("작업 실행 중...");
        });
        t.start();
    }
}
```

### CompletableFuture 사용
```java
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            return "비동기 작업 결과";
        }).thenAccept(result -> {
            System.out.println("결과: " + result);
        }).join();
    }
}
```

### Virtual Threads (Java 21, Project Loom)
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> future = executor.submit(() -> {
        Thread.sleep(1000);
        return "Virtual Thread 실행 완료";
    });
    System.out.println(future.get());
}
```

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

## 4. Kotlin: 언어 수준의 코루틴 지원
Kotlin은 `suspend` 함수와 `CoroutineScope`를 통해 언어 차원에서 코루틴을 지원한다.  
**구조적 동시성**을 지원하여 스코프가 끝나면 관련 코루틴이 자동 취소된다.

### 코루틴 없는 방식
```kotlin
fun main() {
    Thread {
        Thread.sleep(1000)
        println("스레드에서 실행됨")
    }.start()
}
```

### 코루틴 사용
```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        delay(1000)
        println("코루틴에서 실행됨")
    }
}
```

### 구조적 동시성 & 실무 패턴
```kotlin
suspend fun fetchUserData(): String {
    return withContext(Dispatchers.IO) {
        // DB 혹은 API 호출
        "user data"
    }
}

fun main() = runBlocking {
    val data = fetchUserData()
    println("결과: $data")
}
```

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

## 5. JavaScript: 이벤트 루프 기반 비동기
JavaScript는 단일 스레드 언어지만 **이벤트 루프**와 `Promise`, `async/await`를 통해 비동기를 처리한다.

### Promise 사용
```javascript
function getData() {
    return new Promise(resolve => {
        setTimeout(() => resolve("데이터 도착"), 1000);
    });
}

getData().then(result => console.log(result));
```

### async/await (코루틴 스타일)
```javascript
async function main() {
    const result = await getData();
    console.log(result);
}
main();
```

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

## 6. Python: `asyncio` 기반 코루틴
Python은 `async def`와 `await`를 통해 코루틴을 정의하며, `asyncio` 이벤트 루프에서 실행한다.

### 동기 코드
```python
import time

def task():
    time.sleep(1)
    print("작업 완료")

task()
```

### asyncio 기반 코루틴
```python
import asyncio

async def task(name):
    await asyncio.sleep(1)
    print(f"{name} 완료")

async def main():
    await asyncio.gather(task("A"), task("B"))

asyncio.run(main())
```

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

## 7. 정리

- 코루틴은 **가벼운 동시성 모델**  
- 언어마다 구현은 다르지만, 공통 목표는 동일 → "비동기를 동기처럼 쉽게 쓰자"  
- 실무에서는 코루틴을 활용해 **I/O 최적화, 대규모 요청 처리, UI 멈춤 방지** 등을 달성한다.

---

## 8. Java와 Kotlin의 차이점
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
