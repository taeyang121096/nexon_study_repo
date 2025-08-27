// Java: 코루틴 없음 (Thread 이용)
public class ThreadExample {
    public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();

        // 1000개의 스레드 실행
        Thread[] threads = new Thread[1000];
        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Thread.sleep(1000); // 블로킹 I/O 시뮬레이션
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        // 모든 스레드가 끝날 때까지 대기
        for (Thread t : threads) {
            t.join();
        }

    long elapsedMs = System.currentTimeMillis() - start;
    double elapsedSec = elapsedMs / 1000.0;
    System.out.println("Threads elapsed: " + elapsedSec + " sec");
    }
}
