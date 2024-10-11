package thread_pool_workspace;


@FunctionalInterface
public interface RejectPolicy<T> {
    void reject(MyBlockingQueue<T> queue, T task);
}
