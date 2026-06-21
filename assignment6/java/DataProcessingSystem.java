import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataProcessingSystem {

    private static final Logger LOG = Logger.getLogger("DataProcessingSystem");
    private static final int WORKER_COUNT = 4;
    private static final int TASK_COUNT = 20;
    private static final String OUTPUT_FILE = "results.txt";

    static final class Task {
        final int id;
        final int payload;

        Task(int id, int payload) {
            this.id = id;
            this.payload = payload;
        }

        boolean isPoison() { return id < 0; }
        static Task poison() { return new Task(-1, 0); }
    }

    static final class Result {
        final int taskId;
        final long value;
        final String worker;

        Result(int taskId, long value, String worker) {
            this.taskId = taskId;
            this.value = value;
            this.worker = worker;
        }

        @Override
        public String toString() {
            return String.format("task=%d value=%d worker=%s", taskId, value, worker);
        }
    }

    // Thread-safe queue using ReentrantLock + Condition
    static final class TaskQueue {
        private final java.util.ArrayDeque<Task> queue = new java.util.ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();

        void addTask(Task task) {
            lock.lock();
            try {
                queue.addLast(task);
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        Task getTask() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    notEmpty.await();
                }
                return queue.removeFirst();
            } finally {
                lock.unlock();
            }
        }
    }

    static final class Worker implements Runnable {
        private final String name;
        private final TaskQueue queue;
        private final List<Result> results;

        Worker(String name, TaskQueue queue, List<Result> results) {
            this.name = name;
            this.queue = queue;
            this.results = results;
        }

        @Override
        public void run() {
            LOG.info(name + " started");
            try {
                while (true) {
                    Task task = queue.getTask();
                    if (task.isPoison()) {
                        LOG.info(name + " received stop signal, terminating");
                        return;
                    }
                    Result result = process(task);
                    synchronized (results) {
                        results.add(result);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, name + " interrupted", e);
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, name + " unexpected error", e);
            } finally {
                LOG.info(name + " completed");
            }
        }

        private Result process(Task task) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(50);
            long value = (long) task.payload * task.payload;
            return new Result(task.id, value, name);
        }
    }

    public static void main(String[] args) {
        configureLogging();

        TaskQueue queue = new TaskQueue();
        List<Result> results = new ArrayList<>();

        for (int i = 1; i <= TASK_COUNT; i++) {
            queue.addTask(new Task(i, i));
        }
        for (int i = 0; i < WORKER_COUNT; i++) {
            queue.addTask(Task.poison());
        }
        LOG.info("Enqueued " + TASK_COUNT + " tasks and " + WORKER_COUNT + " stop signals");

        ExecutorService pool = Executors.newFixedThreadPool(WORKER_COUNT);
        for (int i = 1; i <= WORKER_COUNT; i++) {
            pool.execute(new Worker("worker-" + i, queue, results));
        }

        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
            LOG.log(Level.SEVERE, "Interrupted while awaiting termination", e);
        }

        writeResults(results);
        LOG.info("All workers done. Processed " + results.size() + " tasks.");
    }

    private static void writeResults(List<Result> results) {
        Path path = Paths.get(OUTPUT_FILE);
        synchronized (results) {
            results.sort((a, b) -> Integer.compare(a.taskId, b.taskId));
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (Result r : results) {
                    writer.write(r.toString());
                    writer.newLine();
                }
                LOG.info("Wrote " + results.size() + " results to " + path.toAbsolutePath());
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to write results to " + path, e);
            }
        }
    }

    private static void configureLogging() {
        LOG.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOG.addHandler(handler);
        LOG.setLevel(Level.INFO);
    }
}
