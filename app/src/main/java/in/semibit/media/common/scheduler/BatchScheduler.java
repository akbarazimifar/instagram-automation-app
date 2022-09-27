package in.semibit.media.common.scheduler;

import androidx.core.util.Pair;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.GenricDataCallback;

public abstract class BatchScheduler {

    String id = "semibitmedia_schedule";
    String tenant = SemibitMediaApp.CURRENT_TENANT;
    Timer timer;
    final Map<String, Long> nextSchedules = new ConcurrentHashMap<>();
    boolean isRunning = false;
    long rateMs = 5 * 60 * 1000;
    long tickCount = 0;
    GenricDataCallback logger = s -> EzUtils.log("FollowerBot BatchScheduler", "" + s);

    public void startScheduler() {
        startScheduler(5 * 60 * 1000);
    }

    public void startScheduler(long tickRateMs) {
        rateMs = tickRateMs;
        tickCount = 0;
        if (timer != null) {
            try {
                timer.cancel();
                timer.purge();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, 0, rateMs);
    }

    public void stopScheduler() {
        try {
            isRunning = false;
            timer.cancel();
            timer.purge();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Pair<String,Instant> getNextJobSchedule() {
        long latest = Long.MAX_VALUE;
        Map.Entry<String ,Long> latestEntry = null;
        for (Map.Entry<String, Long> cur : nextSchedules.entrySet()) {
            if(cur.getValue() < latest){
                latestEntry = cur;
                latest = cur.getValue();
            }
        }
        if (latest == Long.MAX_VALUE)
            return Pair.create("UnknownJob",Instant.now());
        return Pair.create(latestEntry.getKey(),Instant.ofEpochMilli(latestEntry.getValue()));
    }

    public boolean isJobScheduleNowOrPassed(String jobName) {
        if (!nextSchedules.containsKey(jobName)) {
            nextSchedules.put(jobName, Instant.now().minusMillis(1000).toEpochMilli());
        }
        Instant nextJobScheduledTime = Instant.ofEpochMilli(nextSchedules.get(jobName));
        if (nextJobScheduledTime != null) {
            return nextJobScheduledTime.isBefore(Instant.now());
        } else {
            return true;
        }
    }

    public void addToSchedule(String jobName, Instant nextSchedule) {
        nextSchedules.put(jobName, nextSchedule.toEpochMilli());
    }

    public void addAllToSchedule(Map<String, Long> nextSchedules) {
        this.nextSchedules.putAll(nextSchedules);
    }

    public void tick() {
        logger.onStart("Tick " + tickCount++);
        nextSchedules.forEach((jobName, instant) -> {
            if (isJobScheduleNowOrPassed(jobName)) {
                Instant next = startBatchJob(jobName);
                if(next == null){
                    nextSchedules.remove(jobName);
                    return;
                }
                logger.onStart("Triggering batch job " + jobName + " next exec at "
                        + next.atZone(ZoneOffset.systemDefault()).toLocalDateTime().toString());
                addToSchedule(jobName, next);
            }
        });
    }

    /**
     * @param jobName
     * @return next instant to schedule batch . NULL if no next schedule
     */
    public abstract Instant startBatchJob(String jobName);

}
