package in.semibit.media.followerbot.jobs;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.instagram4j.instagram4j.IGClient;
import com.google.firebase.firestore.Source;
import com.semibit.ezandroidutils.EzUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import in.semibit.media.FollowerBotActivity;
import in.semibit.media.MainActivity;
import in.semibit.media.R;
import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.followerbot.FollowerBotForegroundService;
import in.semibit.media.followerbot.FollowerUtil;
import lombok.NonNull;

public class FollowJobOrchestratorV2 {

    public static final boolean TEST_MODE = false;
    public static final String ACTION_BOT_START = "ACTION_BOT_START";
    public static final String ACTION_BOT_STOP = "ACTION_BOT_STOP";

    public Activity context;
    public DatabaseHelper serverDb;
    public DatabaseHelper localDb;
    public View followWidgetView;
    public View unFollowWidgetView;
    public String tenant;
    public FollowerUtil followerUtil;
    GenricDataCallback uiLogger;
    final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();
    boolean isRunning = false;
    IGClient igClient;

    GenricDataCallback logCatLogger = s -> Log.e("FollowerBot", s);

    public FollowJobOrchestratorV2(Activity context, GenricDataCallback uiLogger) {
        this.uiLogger = uiLogger;
        this.context = context;
        serverDb = new DatabaseHelper(Source.SERVER);
        localDb = new DatabaseHelper(Source.CACHE);
        tenant = "semibitmedia";
        getFollowerUtil();
    }

    public GenericCompletableFuture<FollowerUtil> getFollowerUtil() {
        if (followerUtil != null) {
            return GenericCompletableFuture.genericCompletedFuture(followerUtil);
        }
        GenericCompletableFuture<FollowerUtil> future = new GenericCompletableFuture<>();
        AsyncTask.execute(() -> {
            uiLogger.onStart("Please wait for IG Client to initialize");
            igClient = Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), (s) -> {
                uiLogger.onStart("IG Client Ready");
            });
            FollowerUtil followerUtil = new FollowerUtil(igClient, serverDb,uiLogger,context);
            future.complete(followerUtil);
        });
        return future;
    }

    public void killAll(String jobToKill) {
        isRunning = false;
        jobs.forEach((jobName, job) -> {
            if (jobToKill == null || job.getJobName().contains(jobToKill)) {
                job.stop(false);
            }
        });
        try {
            Intent stopIntent = new Intent(context, FollowerBotForegroundService.class);
            stopIntent.setAction(FollowerBotForegroundService.ACTION_STOP);
            context.stopService(stopIntent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
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
        try {
            LocalBroadcastManager.getInstance(broadCastContext).unregisterReceiver(onStartReceive);
            LocalBroadcastManager.getInstance(broadCastContext).unregisterReceiver(onStopReceive);
        } catch (Exception exception) {
            //exception.printStackTrace();
            EzUtils.e("Non fatal error"+ exception.getMessage());
        }

    }

    public void listenToTriggers(View followWidgetView) {
        stopListeningToTriggers();
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
        BatchJob job = jobs.get(jName);
        if (job != null) {
            job.start();
        }
    }

    public IGClient getIgClient() {
        if (igClient == null)
            igClient = Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), null);
        return igClient;
    }


    public View setAndGetView(View view, String viewType) {
        if (viewType.equals("follow")) {
            if (view != null) {
                followWidgetView = view;
            }
            return followWidgetView;
        } else if (viewType.equals("unfollow")) {
            if (view != null) {
                unFollowWidgetView = view;
            }
            return unFollowWidgetView;
        }
        return null;
    }

    public Pair<TextView, AdvancedWebView> generateAlert(final Activity context, String viewType) {

        View followWidget = setAndGetView(null, viewType);

        int layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,   // REMOVE FLAG_NOT_FOCUSABLE
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        WindowManager mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (followWidget != null) {
            try {
                mWindowManager.removeView(followWidget);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        followWidget = LayoutInflater.from(context).inflate(R.layout.follower_bot, null);
        mWindowManager.addView(followWidget, params);


        final TextView label = followWidget.findViewById(R.id.label);
        final AdvancedWebView webView = followWidget.findViewById(R.id.webView);


        View finalWidget = followWidget;
        followWidget.setOnLongClickListener((v) -> {
            mWindowManager.removeView(finalWidget);
            return true;
        });
        followWidget.setOnClickListener(c -> {
            if(webView.getVisibility() == View.VISIBLE){
                webView.setVisibility(View.GONE);
            }
            else {
                webView.setVisibility(View.VISIBLE);
            }
//            Intent intent = new Intent(context, FollowerBotActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
        });
        int MAX_X_MOVE = 10;
        int MAX_Y_MOVE = 10;
        followWidget.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            float mX = params.x;
            float mY = params.y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("AD", "Action E" + event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("AD", "Action Down");
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        if (Math.abs(event.getX() - mX) < MAX_X_MOVE || Math.abs(event.getY() - mY) < MAX_Y_MOVE) {
                            v.performClick();
                        }
                        mX = event.getX();
                        mY = event.getY();

                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("AD", "Action Up");
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
//                            if (isViewCollapsed()) {
//                                collapsedView.setVisibility(View.GONE);
//                                expandedView.setVisibility(View.VISIBLE);
//                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("AD", "Action Move");
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(finalWidget, params);
                        return true;
                }
                return false;
            }
        });
        setAndGetView(followWidget, viewType);
        return Pair.create(label, webView);

    }


    public boolean isRunning() {
        return isRunning;
    }
}
