package in.semibit.media.common.scheduler;

import android.os.AsyncTask;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import in.semibit.media.common.GenricCallback;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.IdentifiedModel;

public abstract class BatchJob<T extends IdentifiedModel, U> {

    private final GenricDataCallback logger;
    private final Queue<T> queue = new ConcurrentLinkedDeque<>();
    private Map<T, JobResult<U>> batchResult = new ConcurrentHashMap<>();
    private boolean isRunning = false;
    private long startTime = 0;

    public BatchJob(GenricDataCallback logger) {
        this.logger = logger;
    }


    /**
     * @param completedItems
     * @return should batch repeat using the same queue
     */
    public abstract boolean onBatchCompleted(Map<T, JobResult<U>> completedItems);

    public abstract GenericCompletableFuture<List<T>> getData();

    public abstract GenericCompletableFuture<JobResult<U>> processItem(T item);


    public boolean isContinueToNext() {
        return isRunning;
    }

    public GenericCompletableFuture<JobResult<U>> postProcess(T item) {
        JobResult<U> result = JobResult.success();
        return (GenericCompletableFuture) GenericCompletableFuture.genericCompletedFuture(result);
    }

    public String getJobName() {
        return this.getClass().getSimpleName();
    }

    public GenricDataCallback getLogger() {
        return logger;
    }

    public Queue<T> getQueue() {
        return queue;
    }

    public void pause() {
        isRunning = false;
        logger.onStart("Job Paused");
    }

    public void stop(boolean withResult) {
        isRunning = false;
        for (T e : getQueue()) {
            Map<T, JobResult<U>> result = new ConcurrentHashMap<>();
            batchResult.put(e, JobResult.failed());
        }
        if (withResult)
            onBatchCompleted(batchResult);
        logger.onStart("Job Stopped");
    }

    public void start() {
        startTime = System.currentTimeMillis();
        isRunning = true;
        getData().thenAccept(data -> {
            logger.onStart(getJobName()+" : Started for "+data.size()+" items");
            getQueue().addAll(data);
            processNextJobItem();
        });
    }

    public void continueJob() {
        processNextJobItem();
    }

    public void runAsync(Runnable callback, int delayMs) {
        if (delayMs <= 0) {
            AsyncTask.execute(callback);
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    callback.run();
                }
            }, delayMs);
        }
    }

    private void processNextJobItem() {
        if (isContinueToNext()) {
            GenricCallback continueJob = () -> {
                final T curItem = getQueue().remove();
                getLogger().onStart("Processing item " + curItem.getId() + " in Job " + getJobName());
                processItem(curItem)
                        .exceptionally(e -> {
                            e.printStackTrace();
                            return new JobResult<U>(JobResult.FAILED_STATUS,null);
                        }).thenAccept(result -> {
                    batchResult.put(curItem, result);
                    postProcess(curItem).thenAccept(e -> {
                        processNextJobItem();
                    });

                });
            };

            if (!getQueue().isEmpty()) {
                continueJob.onStart();
            } else {
                boolean isRepeat = onBatchCompleted(batchResult);
                if (isRepeat) {
                    if (!getQueue().isEmpty()) {
                        continueJob.onStart();
                    }
                } else {
                    long timeTaken = System.currentTimeMillis() - startTime;
                    getLogger().onStart("Batch Job " + getJobName() + " is completed in " + (timeTaken / 1000) + " sec");
                }
            }

        } else {
            getLogger().onStart("Paused job " + getJobName() + " since isContinueToNext returned false");
        }
    }
}
