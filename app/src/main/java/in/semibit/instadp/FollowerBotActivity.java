package in.semibit.instadp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import in.semibit.instadp.common.AdvancedWebView;
import in.semibit.instadp.databinding.ActivityFollowerBotBinding;
import in.semibit.instadp.followerbot.FollowerBotService;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_bot);
        binding = ActivityFollowerBotBinding.bind(findViewById(R.id.root));
        context = this;
        initBot();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW}, 1234);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1112);

        } else {

        }
        binding.searchButton.setOnClickListener(c -> {
            followerBotService.markUsersToFollow(advancedWebView,context);
        });
        binding.searchButton.callOnClick();

    }

    AdvancedWebView advancedWebView ;
    FollowerBotService followerBotService;
    public void initBot(){

        followerBotService = new FollowerBotService(context);
        advancedWebView =  followerBotService.generateAlert(context);
    }
    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}