package in.semibit.media.followerbot;

import android.content.Intent;

import androidx.core.util.Pair;

import com.github.instagram4j.instagram4j.IGClient;
import com.google.firebase.firestore.Source;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.DateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import in.semibit.media.FollowerBotActivity;
import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.BGService;
import in.semibit.media.common.CommonAsyncExecutor;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.scheduler.BatchScheduler;
import in.semibit.media.followerbot.jobs.FollowUsersJob;
import in.semibit.media.followerbot.jobs.FollowUsersViaAPIJob;
import in.semibit.media.followerbot.jobs.UnFollowUsersJob;

public class FollowerBotForegroundService extends BGService {

    public static String ACTION_STOP = "FollowerBotForegroundService_STOP";

    BatchScheduler batchScheduler;
    AtomicInteger jobsInProgress = new AtomicInteger();

    public DatabaseHelper serverDb;
    public DatabaseHelper localDb;
    public FollowerUtil followerUtil;

    @Override
    public void work(Intent entry) {
        String tenant = entry.getStringExtra("tenant");
        if (tenant == null) {
            tenant = SemibitMediaApp.CURRENT_TENANT;
        }
        if (followerUtil == null) {
            serverDb = new DatabaseHelper(Source.SERVER);
            localDb = new DatabaseHelper(Source.CACHE);

            followerUtil = new FollowerUtil(
                    Insta4jClient.getClient(getApplicationContext(), tenant, null),
                    tenant,
                    serverDb, LogsViewModel::addToLog);

            followerUtil = getFollowerUtil(tenant)
                    .exceptionally(e -> {
                        LogsViewModel.addToLog("failed starting BG service " + e.getMessage());
                        return null;
                    }).join();
            if (followerUtil == null) {
                stopWork(entry);
                updateNotification("Failed starting BG service", true);
                stopSelf();
                return;
            }
        }

        batchScheduler = new BatchScheduler() {
            @Override
            public Instant startBatchJob(String jobName) {
                updateNotification(jobsInProgress.incrementAndGet()
                                + " triggered. Next " + getNext()
                        , true);
                Instant nextExec = triggerExecutionOfJob(jobName);
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
        if (jobName.equals(FollowUsersViaAPIJob.JOBNAME)) {
            FollowUsersViaAPIJob job = new FollowUsersViaAPIJob(serverDb, followerUtil);
            job.start();
            return FollowUsersViaAPIJob.nextScheduledTime(Instant.now());
        } else if (jobName.equals(FollowUsersJob.JOBNAME)) {

            Instant nextExec = FollowUsersJob.nextScheduledTime(Instant.now());
            String msg = "Triggering " + jobName + "from BG. Next exec at " + nextExec.atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
            FollowBotService.triggerBroadCast(context.getApplicationContext(), FollowBotService.ACTION_BOT_LOG, msg);
            return nextExec;
        } else if (jobName.equals(UnFollowUsersJob.JOBNAME)) {
            Instant nextExec = UnFollowUsersJob.nextScheduledTime(Instant.now());
            String msg = "Triggering " + jobName + "from BG. Next exec at " + nextExec.atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
            FollowBotService.triggerBroadCast(context.getApplicationContext(), FollowBotService.ACTION_BOT_LOG, msg);
            return nextExec;
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


    public CompletableFuture<FollowerUtil> getFollowerUtil(String tenant) {
        if (followerUtil != null) {
            return CompletableFuture.completedFuture(followerUtil);
        }
        LogsViewModel.addToLog("Please wait for IG Client to initialize");

        CompletableFuture<FollowerUtil> future = new CompletableFuture<>();
        CommonAsyncExecutor.execute(() -> {
            IGClient igClient = Insta4jClient.getClient(context, tenant, (s) -> {
            });
            LogsViewModel.addToLog("IG Client Ready");
            FollowerUtil followerUtil = new FollowerUtil(igClient, tenant, serverDb, LogsViewModel::addToLog);
            future.complete(followerUtil);
        });
        return future;
    }
}
