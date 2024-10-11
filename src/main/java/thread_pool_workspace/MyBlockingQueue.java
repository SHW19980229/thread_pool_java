package thread_pool_workspace;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {

    //队列
    private Deque<T> queue = new ArrayDeque<>();

    //互斥锁
    private ReentrantLock lock = new ReentrantLock();

    //条件变量 队列满
    private Condition full = lock.newCondition();
    //条件变量 队列空
    private Condition empty = lock.newCondition();
    //容量
    private int capcity;

    public MyBlockingQueue(int capcity) {
        this.capcity = capcity;
    }

    //添加元素
    public void put(T task) {
        lock.lock();
        try {
            while (queue.size() == capcity) {
                try {
                    full.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addLast(task);
            empty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(T task, long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (queue.size() == capcity) {
                try {
                    if (nanos <= 0)
                        return false;
                    nanos = full.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addLast(task);
            empty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T t=queue.removeFirst();
            full.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    public T poll(long timeout,TimeUnit timeUnit){
        lock.lock();
        try{
            long nanos=timeUnit.toNanos(timeout);
            while(queue.isEmpty()){
                try {
                    if(nanos<=0)
                        return null;
                    nanos=empty.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T t=queue.removeFirst();
            empty.signal();
            return t;
        }finally {
            lock.unlock();
        }
    }

    public int size(){
        lock.lock();
        try{
            return queue.size();
        }finally {
            lock.unlock();
        }
    }

    public void tryPut(RejectPolicy<T> rejectPolicy,T task){
        lock.lock();
        try{
            if(queue.size()==capcity){
                rejectPolicy.reject(this,task);
            }else{
                queue.addLast(task);
                empty.signal();
            }
        }finally {
            lock.unlock();
        }
    }


}
