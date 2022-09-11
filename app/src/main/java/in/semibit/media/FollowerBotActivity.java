package in.semibit.media;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.gms.common.util.Strings;
import com.semibit.ezandroidutils.EzUtils;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.databinding.ActivityFollowerBotBinding;
import in.semibit.media.followerbot.FollowerBotService;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Activity context;

    GenricDataCallback logger = new GenricDataCallback() {
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
            followerBotService.syncConnectionsForUserToFirebase(context.getString(R.string.username), false, new GenricDataCallback() {
                @Override
                public void onStart(String s) {
                    context.runOnUiThread(() -> binding.refresh.setEnabled(true));
                }
            }, logger);

            followerBotService.syncConnectionsForUserToFirebase(context.getString(R.string.username), true, new GenricDataCallback() {
                @Override
                public void onStart(String s) {
                    context.runOnUiThread(() -> binding.refresh.setEnabled(true));
                }
            }, logger);
        });

        binding.clearLogs.setOnLongClickListener(c -> {
            EzUtils.toast(context, "Clear Logs");
            return true;
        });

        binding.clearLogs.setOnClickListener(c -> {
            binding.logs.setText("");
        });

        binding.showHideBot.setOnClickListener((c) -> {
            if (followerBotService != null) {
                try {
                    if (followerBotService.followWidget != null) {
                        int visiv = followerBotService.followWidget.findViewById(R.id.webView).getVisibility();
                        if (visiv == View.GONE) {
                            binding.showHideBot.setText("HIDE BOT");
                            followerBotService.followWidget.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                            followerBotService.unFollowWidget.findViewById(R.id.webView).setVisibility(View.VISIBLE);
                        }
                        else {
                            binding.showHideBot.setText("SHOW BOT");
                            followerBotService.followWidget.findViewById(R.id.webView).setVisibility(View.GONE);
                            followerBotService.unFollowWidget.findViewById(R.id.webView).setVisibility(View.GONE);
                        }
                    }
                    else {
                        logger.onStart("Bot windows not initialized yet");
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    logger.onStart("Error showing windows "+exception.getMessage());
                }
            }
        });

        binding.startBot.setOnLongClickListener(c -> {
            EzUtils.toast(context, "Start/Stop FollowerBot");
            return true;
        });
        binding.startBot.setOnClickListener(c -> {
            if (followerBotService != null) {
                if (!followerBotService.isRunning()) {

                    followWebView = followerBotService.generateAlert(context, "follow");
                    unfollowWebView = followerBotService.generateAlert(context, "unfollow");
                    followerBotService.getUsersToBeFollowed(logger);
                    followerBotService.getUsersToBeUnFollowed(logger,true);

                    followerBotService.cronStart(followWebView, unfollowWebView, logger);
                } else
                    followerBotService.kill(logger);
                updateButtonState();
            } else {
                logger.onStart("FollowerBotService Not Initialized yet");
            }
        });

        binding.searchButton.setOnClickListener(c -> {
            if (followerBotService != null) {

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
                        followerBotService.markUsersToFollowFromPost(shortCode, new GenricDataCallback() {
                            @Override
                            public void onStart(String s) {

                            }
                        }, logger);
                        binding.conturlOrUsername.setError(null);
                        return;
                    }
                } else {
                    String userName = urlOrUname;
                    if (urlOrUname.contains("instagram.com") && split.length > 1) {
                        userName = split[1];
                    }
                    followerBotService.markUsersToFollowFromFollowers(userName, new GenricDataCallback() {
                        @Override
                        public void onStart(String s) {

                        }
                    }, logger);
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
    static FollowerBotService followerBotService;

    public void initBot() {

        if (followerBotService == null) {
            logger.onStart("Initialized FollowerBotService");
            followerBotService = new FollowerBotService(context);
//            followWebView = followerBotService.generateAlert(context, "follow");
//            unfollowWebView = followerBotService.generateAlert(context, "unfollow");
            followerBotService.getUsersToBeFollowed(logger);
            followerBotService.getUsersToBeUnFollowed(logger, true);

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

    private void updateButtonState() {
        try {
            if (followerBotService.isRunning()) {
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