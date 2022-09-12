package in.semibit.media.followerbot;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import in.semibit.media.common.BGService;
import in.semibit.media.common.scheduler.BatchScheduler;
import in.semibit.media.followerbot.jobs.FollowUsersJob;
import in.semibit.media.followerbot.jobs.UnFollowUsersJob;

public class FollowerBotForegroundService extends BGService {

    public static String ACTION_STOP ="FollowerBotForegroundService_STOP";

    FollowUsersJob followUsersJob;
    BatchScheduler batchScheduler;
    AtomicInteger jobsInProgress = new AtomicInteger();

    @Override
    public void work(Intent entry) {
        batchScheduler = new BatchScheduler() {
            @Override
            public Instant startBatchJob(String jobName) {
                updateNotification(jobsInProgress.incrementAndGet()+" jobs triggered",true);

                Instant nextExec =  triggerExecutionOfJob(jobName);
                String msg = "Triggering "+jobName+"from BG. Next exec at "+nextExec.atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
                FollowBotService.triggerBroadCast(context, FollowBotService.ACTION_BOT_LOG,msg);

                return nextExec;
            }
        };
        String jstrJobs = entry.getStringExtra("jobSchedules");
        if(jstrJobs == null || jstrJobs.isEmpty()){
            updateNotification("No batch jobs provided via intent",true);
            return;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Long>>() {}.getType();
        Map<String, Long> jobSchedules = gson.fromJson(jstrJobs, type);
        batchScheduler.addAllToSchedule(jobSchedules);
        batchScheduler.startScheduler(3000);
    }

    public Instant triggerExecutionOfJob(String jobName) {
        FollowBotService.triggerBroadCast(context, FollowBotService.ACTION_BOT_START,jobName);

        if (jobName.equals(FollowUsersJob.JOBNAME)) {
            return FollowUsersJob.nextScheduledTime(Instant.now());
        } else if (jobName.equals(UnFollowUsersJob.JOBNAME)) {
            return UnFollowUsersJob.nextScheduledTime(Instant.now());
        }
        return null;
    }

    @Override
    public void stopWork(Intent intent) {
        if (followUsersJob != null) {
            followUsersJob.stop(true);
        }
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
}
