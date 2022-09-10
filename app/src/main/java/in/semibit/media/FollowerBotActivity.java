package in.semibit.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.databinding.ActivityFollowerBotBinding;
import in.semibit.media.followerbot.FollowerBotService;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Activity context;

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
        binding.searchButton.setOnClickListener(c -> {
            if (followerBotService != null)
                followerBotService.markUsersToFollowFromFollowers("the_engineer_bro", new GenricDataCallback() {
                    @Override
                    public void onStart(String s) {

                    }
                });
        });
        binding.searchButton.callOnClick();

    }

    AdvancedWebView advancedWebView;
    FollowerBotService followerBotService;

    public void initBot() {

        followerBotService = new FollowerBotService(context);
//        advancedWebView = followerBotService.generateAlert(context);
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
}