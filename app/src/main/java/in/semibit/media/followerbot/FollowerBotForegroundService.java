package in.semibit.media.followerbot;

import android.content.Intent;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import in.semibit.media.FollowerBotActivity;
import in.semibit.media.common.BGService;
import in.semibit.media.common.scheduler.BatchScheduler;
import in.semibit.media.followerbot.jobs.FollowUsersJob;
import in.semibit.media.followerbot.jobs.UnFollowUsersJob;

public class FollowerBotForegroundService extends BGService {

    public static String ACTION_STOP = "FollowerBotForegroundService_STOP";

    BatchScheduler batchScheduler;
    AtomicInteger jobsInProgress = new AtomicInteger();

    @Override
    public void work(Intent entry) {
        batchScheduler = new BatchScheduler() {
            @Override
            public Instant startBatchJob(String jobName) {
                updateNotification(jobsInProgress.incrementAndGet()
                                + " triggered. Next " + getNext()
                        , true);

                Instant nextExec = triggerExecutionOfJob(jobName);
                String msg = "Triggering " + jobName + "from BG. Next exec at " + nextExec.atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
                FollowBotService.triggerBroadCast(context.getApplicationContext(), FollowBotService.ACTION_BOT_LOG, msg);

                return nextExec;
            }
        };
        String jstrJobs = entry.getStringExtra("jobSchedules");
        if (jstrJobs == null || jstrJobs.isEmpty()) {
            updateNotification("No batch jobs provided via intent", true);
            return;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Long>>() {
        }.getType();
        Map<String, Long> jobSchedules = gson.fromJson(jstrJobs, type);
        batchScheduler.addAllToSchedule(jobSchedules);
        batchScheduler.startScheduler(3000);
        updateNotification("Next " + getNext(), true);
    }

    public String getNext() {
        Pair<String, Instant> next = batchScheduler.getNextJobSchedule();
        return
                next.first + " after " +
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                                .format(Date.from(next.second));
    }

    public Instant triggerExecutionOfJob(String jobName) {
        FollowBotService.triggerBroadCast(context, FollowBotService.ACTION_BOT_START, jobName);

        if (jobName.equals(FollowUsersJob.JOBNAME)) {
            return FollowUsersJob.nextScheduledTime(Instant.now());
        } else if (jobName.equals(UnFollowUsersJob.JOBNAME)) {
            return UnFollowUsersJob.nextScheduledTime(Instant.now());
        }
        return null;
    }

    @Override
    public void stopWork(Intent intent) {
        if (batchScheduler != null)
            batchScheduler.stopScheduler();
    }

    @Override
    public int getNotificationId() {
        return 8291;
    }

    @Override
    public String getActionStopId() {
        return ACTION_STOP;
    }


    @Override
    public String getServiceName() {
        return "Follower Service";
    }

    @Override
    protected Class<?> getOverriddenClass() {
        return FollowerBotForegroundService.class;
    }

    @Override
    public void onDestroy() {
        batchScheduler.stopScheduler();
        super.onDestroy();
    }

    @Override
    protected Intent getOnTouchIntent() {
        Intent intent = new Intent(context, FollowerBotActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
