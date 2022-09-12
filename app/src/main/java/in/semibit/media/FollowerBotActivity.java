package in.semibit.media;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.databinding.ActivityFollowerBotBinding;
import in.semibit.media.followerbot.FollowerBotForegroundService;
import in.semibit.media.followerbot.FollowBotService;
import in.semibit.media.followerbot.FollowerUtil;
import in.semibit.media.followerbot.jobs.FollowUsersJob;
import in.semibit.media.followerbot.jobs.UnFollowUsersJob;
import in.semibit.media.followerbot.jobs.UserFromFollowerMarkerJob;
import in.semibit.media.followerbot.jobs.UserFromPostMarkerJob;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Activity context;
    FollowerUtil followerUtil;

    public GenricDataCallback logger = new GenricDataCallback() {
        @Override
        public void onStart(String s) {
            context.runOnUiThread(() -> {
                binding.logs.append("\n" + s);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_bot);
        binding = ActivityFollowerBotBinding.bind(findViewById(R.id.root));
        context = this;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW}, 1234);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1112);

        } else {
            initBot();
        }

        binding.refresh.setOnLongClickListener(c -> {
            EzUtils.toast(context, "Sync followers from Instagram");
            return true;
        });
        binding.refresh.setOnClickListener(v -> {
            binding.refresh.setEnabled(false);
            logger.onStart("Refresh From IG Started");
            followerBotOrchestrator.getFollowerUtil().thenAccept(e->{
                e.syncConnectionsForUserToFirebase(context.getString(R.string.username),
                        false, s -> context.runOnUiThread(() -> binding.refresh.setEnabled(true)), logger);

                e.syncConnectionsForUserToFirebase(context.getString(R.string.username),
                        true, s -> context.runOnUiThread(() -> binding.refresh.setEnabled(true)), logger);
            });
        });

        binding.clearLogs.setOnLongClickListener(c -> {
            EzUtils.toast(context, "Clear Logs");
            return true;
        });

        binding.clearLogs.setOnClickListener(c -> {
            binding.logs.setText("");
        });

        binding.showHideBot.setOnClickListener((c) -> {
            if (followerBotOrchestrator != null) {
                try {
                    if (followerBotOrchestrator.followWidgetView != null) {
                        int visiv = followerBotOrchestrator.followWidgetView.findViewById(R.id.webView).getVisibility();
                        if (visiv == View.GONE) {
                            binding.showHideBot.setText("HIDE BOT");
                            followerBotOrchestrator.followWidgetView.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                            followerBotOrchestrator.unFollowWidgetView.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                        } else {
                            binding.showHideBot.setText("SHOW BOT");
                            followerBotOrchestrator.followWidgetView.findViewById(R.id.webView).setVisibility(View.GONE);
                            followerBotOrchestrator.unFollowWidgetView.findViewById(R.id.webView).setVisibility(View.GONE);
                        }
                    } else {
                        logger.onStart("Bot windows not initialized yet");
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    logger.onStart("Error showing windows " + exception.getMessage());
                }
            }
        });

        binding.startBot.setOnLongClickListener(c -> {
            EzUtils.toast(context, "Start/Stop FollowerBot");
            return true;
        });
        binding.startBot.setOnClickListener(c -> {
            if (followerBotOrchestrator != null) {
                if (!followerBotOrchestrator.isRunning()) {
                    if(followerUtil == null){
                        ;
                        followerBotOrchestrator.getFollowerUtil().thenAccept(futil->{
                            followerUtil = futil;
                        });
                        return;
                    }

                    followWebView = followerBotOrchestrator.generateAlert(context, "follow");
                    unfollowWebView = followerBotOrchestrator.generateAlert(context, "unfollow");

                    followerBotOrchestrator.addBatchJob(new FollowUsersJob(logger, followWebView, followerBotOrchestrator.serverDb,followerUtil,context));
                    followerBotOrchestrator.addBatchJob(new UnFollowUsersJob(logger, unfollowWebView, followerBotOrchestrator.serverDb,followerUtil,context));

                    followerBotOrchestrator.listenToTriggers(followWebView.second);
                    Intent intent = new Intent(context, FollowerBotForegroundService.class);
                    Map<String, Long> jobs = new ConcurrentHashMap<>();
                    jobs.put(FollowUsersJob.JOBNAME, FollowUsersJob.nextScheduledTime(Instant.now()).toEpochMilli());
                    jobs.put(UnFollowUsersJob.JOBNAME, UnFollowUsersJob.nextScheduledTime(Instant.now()).toEpochMilli());
                    intent.putExtra("jobSchedules", new Gson().toJson(jobs));
                    startForegroundService(intent);

                    FollowBotService.triggerBroadCast(this, FollowBotService.ACTION_BOT_START,FollowUsersJob.JOBNAME);
                    FollowBotService.triggerBroadCast(this, FollowBotService.ACTION_BOT_START,UnFollowUsersJob.JOBNAME);

                } else
                    followerBotOrchestrator.killAll(null);
               new Handler().postDelayed(this::updateButtonState,1000);
            } else {
                logger.onStart("FollowerBotService Not Initialized yet");
            }
        });

        binding.searchButton.setOnClickListener(c -> {
            if (followerBotOrchestrator != null) {

                if (binding.urlOrUsername.getText() == null || Strings.isEmptyOrWhitespace(binding.urlOrUsername.getText().toString())) {
                    binding.conturlOrUsername.setError("Please enter post url");

                    try {
                        String textToPaste = MainActivity.readFromClipboard(this);
                        if (textToPaste.contains("instagram.com/")) {
                            binding.urlOrUsername.setText(textToPaste);
                            binding.conturlOrUsername.setError(null);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                binding.conturlOrUsername.setError(null);

                String urlOrUname = binding.urlOrUsername.getText().toString();
                String split[] = urlOrUname.replace("https://", "").split("/");

                if ((urlOrUname.contains("/p/") || urlOrUname.contains("/reel/")) && urlOrUname.contains("instagram.com")) {
                    if (split.length > 2) {
                        String shortCode = split[2];
                        UserFromFollowerMarkerJob followerMarkerJob = new UserFromFollowerMarkerJob(logger, followerBotOrchestrator.serverDb, followerUtil);
                        followerMarkerJob.markUsersToFollowFromPost(shortCode, logger, logger);
                        binding.conturlOrUsername.setError(null);
                        return;
                    }
                } else {
                    String userName = urlOrUname;
                    if (urlOrUname.contains("instagram.com") && split.length > 1) {
                        userName = split[1];
                    }
                    UserFromPostMarkerJob followerMarkerJob = new UserFromPostMarkerJob(logger, followerBotOrchestrator.serverDb, followerUtil);
                    followerMarkerJob.markUsersToFollowFromFollowers(userName, logger, logger);
                    binding.conturlOrUsername.setError(null);
                    return;
                }
                binding.conturlOrUsername.setError("Invalid Input");

            } else {
                MainActivity.toast(context, "FollowBotService Not initialized. Wait a min...");
                initBot();
            }
        });
//        binding.searchButton.callOnClick();

    }

    Pair<TextView, AdvancedWebView> followWebView;
    Pair<TextView, AdvancedWebView> unfollowWebView;

    FollowBotService followerBotOrchestrator;

    public void initBot() {

        if (followerBotOrchestrator == null) {

            followerBotOrchestrator = new FollowBotService(FollowerBotActivity.this, logger);
            GenericCompletableFuture<FollowerUtil> onFollowerUtil = followerBotOrchestrator.getFollowerUtil();
            ;
            onFollowerUtil.thenAccept(u -> {
                followerUtil = u;
            });


        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initBot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonState();
    }

    @Override
    protected void onDestroy() {
        followerBotOrchestrator.stopListeningToTriggers();
        super.onDestroy();
    }

    private void updateButtonState() {
        try {
            if (followerBotOrchestrator.isRunning()) {
                binding.startBot.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_red_500));
                binding.startBot.setText("STOP");
            } else {
                binding.startBot.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_green_500));
                binding.startBot.setText("START");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}