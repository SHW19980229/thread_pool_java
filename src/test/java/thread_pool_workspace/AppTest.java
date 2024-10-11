package thread_pool_workspace;


import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;



public class AppTest {

    @Test
    void Test() {
        MyThreadPool threadPool = new MyThreadPool(3, 2, TimeUnit.SECONDS, 5, new RejectPolicy<Runnable>() {
            @Override
            public void reject(MyBlockingQueue<Runnable> queue, Runnable task) {
                System.out.println("拒绝");
            }
        });
        threadPool.execute(new Runnable() {
            public void run() {
                System.out.println("工作完成");
            }
        });

    }

}
