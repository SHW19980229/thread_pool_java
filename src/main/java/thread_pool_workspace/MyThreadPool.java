package thread_pool_workspace;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    private MyBlockingQueue<Runnable> taskQueue;

    //核心线程数
    private int coreSize;

    private long timeout;

    private TimeUnit timeUnit;

    private RejectPolicy<Runnable> rejectPolicy;

    public MyThreadPool(int coreSize,long timeout,TimeUnit timeUnit,int queueCapcity,RejectPolicy<Runnable> rejectPolicy){
        this.coreSize=coreSize;
        this.timeout=timeout;
        this.timeUnit=timeUnit;
        this.taskQueue=new MyBlockingQueue<>(queueCapcity);
        this.rejectPolicy=rejectPolicy;
    }

    private HashSet<Worker> workers = new HashSet<>();

    public void execute(Runnable task) {
        synchronized (workers) {
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                workers.add(worker);
                worker.start();
            } else {
                taskQueue.tryPut(rejectPolicy,task);
            }
        }
    }


    public class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {

            while (task != null || (task = taskQueue.poll(timeout, timeUnit)) != null) {
                try {
                    System.out.println("Worker工作");
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (workers){
                System.out.println("Worker移除");
                workers.remove(this);
            }
        }
    }

}
