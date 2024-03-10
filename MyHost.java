/* Implement this class. */
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

public class MyHost extends Host {

    private PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>(100, new TaskPriorityComparator());
    private final Semaphore semaphore = new Semaphore(0);
    private volatile boolean isRunning = true;
    private Task currentTask = null;
    private volatile boolean isInterrupted = false;

    private AtomicLong totalWorkLeft = new AtomicLong(0);

    private volatile boolean isExecuted = false;

    @Override
    public void run() {
        while (isRunning) {
            try {
                semaphore.acquire();
                currentTask = queue.poll();
                if (currentTask != null) {
                    isExecuted = true;
                    while(currentTask.getLeft() > 0) {
                        Thread.sleep(1000);
                        currentTask.setLeft(currentTask.getLeft() - 1000);
                        totalWorkLeft.addAndGet(-1000);

                        if (isInterrupted) {
                            break;
                        }
                    }
                    currentTask.finish();
                    isInterrupted = false;
                }
                isExecuted = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void addTask(Task task) {
        queue.add(task);
        totalWorkLeft.addAndGet(task.getDuration());
        semaphore.release();
    }

    @Override
    public int getQueueSize() {
        if(isExecuted)
            return queue.size() + 10000;
        else return queue.size();
    }

    @Override
    public long getWorkLeft() {
        if(isExecuted)
            return totalWorkLeft.get() + 10000;
        else return totalWorkLeft.get();
    }

    @Override
    public void shutdown() {
        isRunning = false;
        interrupt();
    }

    public Task getCurrentTask() {
        return currentTask;
    }
    public void preemptCurrentTask() {
        isInterrupted = true;
    }

    private static class TaskPriorityComparator implements Comparator<Task> {
        @Override
        public int compare(Task task1, Task task2) {
            int comparison = Integer.compare(task2.getPriority(), task1.getPriority());

            if(comparison == 0) {
                return  Long.compare(task1.getStart(), task2.getStart());
            } else return comparison;

        }
    }
}

