import threading, time, asyncio

# 스레드 사용
def blocking_task():
    time.sleep(1)

def threads_example():
    start = time.time()
    threads = [threading.Thread(target=blocking_task) for _ in range(10000)]
    for t in threads: t.start()
    for t in threads: t.join()
    print("Threads elapsed:", time.time() - start)

# 코루틴 사용
async def async_task():
    await asyncio.sleep(1)

async def coroutines_example():
    start = time.time()
    await asyncio.gather(*(async_task() for _ in range(100000)))
    print("Coroutines elapsed:", time.time() - start)

threads_example()
asyncio.run(coroutines_example())
