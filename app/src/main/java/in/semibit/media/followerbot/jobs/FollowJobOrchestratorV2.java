package in.semibit.media.followerbot.jobs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.firestore.Source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.scheduler.BatchJob;
import lombok.NonNull;

public class FollowJobOrchestratorV2 {

    private static FollowJobOrchestratorV2 followJobOrchestratorV2;

    public static final String ACTION_BOT_START = "ACTION_BOT_START";
    public static final String ACTION_BOT_STOP = "ACTION_BOT_STOP";


    public Activity context;
    public DatabaseHelper serverDb;
    public DatabaseHelper localDb;
    public View followWidgetView;
    public View unFollowWidgetView;
    public String tenant;
    GenricDataCallback uiLogger;
    final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();
    boolean isRunning = false;

    GenricDataCallback logCatLogger = s -> Log.e("FollowerBot", s);


    public FollowJobOrchestratorV2(Activity context, GenricDataCallback uiLogger) {
        this.uiLogger = uiLogger;
        this.context = context;
        serverDb = new DatabaseHelper(Source.SERVER);
        localDb = new DatabaseHelper(Source.CACHE);
        tenant = "semibitmedia";
    }

    public void killAll(String jobToKill) {
        jobs.forEach((jobName, job) -> {
            if (jobToKill == null || job.getJobName().contains(jobToKill)) {
                job.stop(false);
            }
        });
    }

    private BroadcastReceiver onStartReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String jobName = intent.getStringExtra("jobName");
            if (jobName != null) {
                singleStart(jobName);
            }
        }
    };

    Context broadCastContext;
    private BroadcastReceiver onStopReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            softKill();
        }
    };

    public void softKill() {
        uiLogger.onStart("Soft Killed UI Service");
        isRunning = false;
        try {
            jobs.values().stream().forEach(job -> {
                job.stop(false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopListeningToTriggers() {
        LocalBroadcastManager.getInstance(broadCastContext).unregisterReceiver(onStartReceive);
        LocalBroadcastManager.getInstance(broadCastContext).unregisterReceiver(onStopReceive);

    }

    public void listenToTriggers(View followWidgetView) {
        broadCastContext = followWidgetView.getContext();
        LocalBroadcastManager.getInstance(broadCastContext).registerReceiver(onStartReceive, new IntentFilter(ACTION_BOT_START));
        LocalBroadcastManager.getInstance(broadCastContext).registerReceiver(onStopReceive, new IntentFilter(ACTION_BOT_STOP));
    }

    public static void triggerBroadCast(@NonNull Context context, @NonNull String action, @Nullable String jobName) {
        Intent intent = new Intent(action);
        if (jobName != null)
            intent.putExtra("jobName", jobName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void addBatchJob(BatchJob batchJob) {
        jobs.put(batchJob.getJobName(), batchJob);
    }

    public void singleStart(String jName) {
        isRunning = true;
        jobs.forEach((jobName, batchJob) -> triggerBroadCast(context, ACTION_BOT_START, jobName));
        BatchJob job = jobs.get(jName);
        if (job != null) {
            job.start();
        }
    }


}
