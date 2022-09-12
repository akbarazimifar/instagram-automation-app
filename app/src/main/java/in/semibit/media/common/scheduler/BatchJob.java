package in.semibit.media.common.scheduler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.IdentifiedModel;

public abstract class BatchJob<T extends IdentifiedModel> {

    private GenricDataCallback logger;
    private Queue<T> queue = new ConcurrentLinkedDeque<>();
    private boolean isRunning = false;

    public BatchJob(GenricDataCallback logger) {
        this.logger = logger;
    }

    abstract GenericCompletableFuture<T> getData();
    abstract <U> GenericCompletableFuture<JobResult<U>> processItem(T item);


    boolean isContinueToNext() {
        return isRunning;
    }

    public <U> GenericCompletableFuture<JobResult<U>> postProcess(T item) {
        JobResult<U> result = JobResult.success();
        return (GenericCompletableFuture) GenericCompletableFuture.completedFuture(result);
    }

    public String getJobName() {
        return getClass().getName();
    }

    public GenricDataCallback getLogger() {
        return logger;
    }

    public Queue<T> getQueue() {
        return queue;
    }

    public void stop() {
        isRunning = false;
        logger.onStart("Job Stopped");
    }

    public void start() {
        isRunning = true;
        processNextJobItem();
    }

    private void processNextJobItem() {
        if (isContinueToNext() && !getQueue().isEmpty()) {
            T curItem = getQueue().remove();
            getLogger().onStart("Processing item " + curItem.getId() + " in Job " + getJobName());
            processItem(curItem)
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return JobResult.failed();
                    }).thenAccept(result -> {

                postProcess(curItem).thenAccept(e -> {
                    processNextJobItem();
                });

            });
        } else {
            getLogger().onStart("Paused job " + getJobName() + (!getQueue().isEmpty() ? "" : "since queue is empty"));
        }
    }

    ;
}
