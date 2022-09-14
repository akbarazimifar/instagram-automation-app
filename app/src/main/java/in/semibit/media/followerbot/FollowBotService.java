package in.semibit.media.followerbot;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import in.semibit.media.R;
import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.SignalLiveData;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.scheduler.BatchJob;
import lombok.NonNull;

public class FollowBotService {

    public static final boolean TEST_MODE = true;
    public static final String ACTION_BOT_START = "ACTION_BOT_START";
    public static final String ACTION_BOT_STOP = "ACTION_BOT_STOP";
    public static final String ACTION_BOT_LOG = "ACTION_BOT_LOG";

    public Activity context;
    public DatabaseHelper serverDb;
    public DatabaseHelper localDb;
    public final Map<String, View> widgetsMap = new HashMap<>();
    public String tenant;
    public FollowerUtil followerUtil;
    GenricDataCallback uiLogger;
    final Map<String, BatchJob> jobs = new ConcurrentHashMap<>();
    boolean isRunning = false;
    IGClient igClient;

    GenricDataCallback logCatLogger = s -> Log.e("FollowerBot", s);

    public FollowBotService(Activity context, GenricDataCallback uiLogger) {
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
        uiLogger.onStart("Please wait for IG Client to initialize");

        GenericCompletableFuture<FollowerUtil> future = new GenericCompletableFuture<>();
        AsyncTask.execute(() -> {
            igClient = Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), (s) -> {
            });
            uiLogger.onStart("IG Client Ready");
            FollowerUtil followerUtil = new FollowerUtil(igClient, serverDb, uiLogger);
            future.complete(followerUtil);
        });
        return future;
    }

    public void killAll(String jobToKill) {
        isRunning = false;
        jobs.forEach((jobName, job) -> {
            if (jobToKill == null || job.getJobName().contains(jobToKill)) {
                job.stop(true);
            }
        });
        jobs.clear();
        try {
            Intent stopIntent = new Intent(context, FollowerBotForegroundService.class);
            stopIntent.setAction(FollowerBotForegroundService.ACTION_STOP);
            context.stopService(stopIntent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            WindowManager mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            for (View followWidget : widgetsMap.values())
                if (followWidget != null) {
                    try {
                        mWindowManager.removeView(followWidget);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            widgetsMap.clear();
            uiLogger.onStart("Bot views removed from window");

        } catch (Exception e) {
            e.printStackTrace();
            uiLogger.onStart("Error removing views " + e.getMessage());
        }
    }

    private BroadcastReceiver onStartReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(@Nullable Context context, Intent intent) {
            String jobName = intent.getStringExtra("jobName");
            if (jobName != null) {
                singleStart(jobName);
            }
        }
    };

    Context broadCastContext;
    private BroadcastReceiver onStopReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(@Nullable Context context, Intent intent) {
            if(intent.getStringExtra("jobName") !=null && intent.getStringExtra("jobName").equals(ACTION_BOT_STOP))
            softKill();
        }
    };
    private BroadcastReceiver onLogRecieve = new BroadcastReceiver() {
        @Override
        public void onReceive(@Nullable Context context, Intent intent) {
            if (uiLogger != null) {
                uiLogger.onStart("" + intent.getStringExtra("jobName"));
            }
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
            LocalBroadcastManager.getInstance(broadCastContext).unregisterReceiver(onLogRecieve);

            SignalLiveData.getLiveLogData().removeUnsafeObserver(ACTION_BOT_START);
            SignalLiveData.getLiveLogData().removeUnsafeObserver(ACTION_BOT_STOP);

        } catch (Exception exception) {
            //exception.printStackTrace();
            EzUtils.e("Non fatal error" + exception.getMessage());
        }

    }

    public void listenToTriggers(View followWidgetView) {
        stopListeningToTriggers();
        broadCastContext = followWidgetView.getContext().getApplicationContext();
        LocalBroadcastManager.getInstance(broadCastContext).registerReceiver(onStartReceive, new IntentFilter(ACTION_BOT_START));
        LocalBroadcastManager.getInstance(broadCastContext).registerReceiver(onStopReceive, new IntentFilter(ACTION_BOT_STOP));
        LocalBroadcastManager.getInstance(broadCastContext).registerReceiver(onLogRecieve, new IntentFilter(ACTION_BOT_LOG));

        SignalLiveData.getLiveLogData().addUnsafeObserver(ACTION_BOT_START, onStartReceive);
        SignalLiveData.getLiveLogData().addUnsafeObserver(ACTION_BOT_STOP, onStopReceive);

    }

    public static void triggerBroadCast(@NonNull Context context, @NonNull String action, @Nullable String jobName) {
        Intent intent = new Intent(action);
        if (jobName != null)
            intent.putExtra("jobName", jobName);
        LogsViewModel.addToLog(jobName + " Triggered from background service");
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        SignalLiveData.getLiveLogData().postSingleValue(jobName);
    }

    public void addBatchJob(BatchJob batchJob) {
        jobs.put(batchJob.getJobName(), batchJob);
    }

    public void singleStart(String jobName) {
        LogsViewModel.addToLog(jobName + " Trigger Received in UI");
        isRunning = true;
        BatchJob job = jobs.get(jobName);
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
        if (view != null) {
            widgetsMap.put(viewType, view);
        }
        return widgetsMap.get(viewType);
    }

    public Pair<TextView, AdvancedWebView> generateAlert(final Activity context, String viewType) {

        View curViewRoot = setAndGetView(null, viewType);

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
        if (curViewRoot != null) {
            try {
                mWindowManager.removeView(curViewRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        curViewRoot = LayoutInflater.from(context).inflate(R.layout.follower_bot, null);
        mWindowManager.addView(curViewRoot, params);


        final TextView label = curViewRoot.findViewById(R.id.label);
        final AdvancedWebView webView = curViewRoot.findViewById(R.id.webView);


        View finalWidget = curViewRoot;
        curViewRoot.setOnLongClickListener((v) -> {
            mWindowManager.removeView(finalWidget);
            return true;
        });
        curViewRoot.setOnClickListener(c -> {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.setVisibility(View.GONE);
            } else {
                webView.setVisibility(View.VISIBLE);
            }
//            Intent intent = new Intent(context, FollowerBotActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
        });
        int MAX_X_MOVE = 10;
        int MAX_Y_MOVE = 10;
        curViewRoot.setOnTouchListener(new View.OnTouchListener() {
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
        setAndGetView(curViewRoot, viewType);
        return Pair.create(label, webView);

    }


    public boolean isRunning() {
        return isRunning;
    }
}
