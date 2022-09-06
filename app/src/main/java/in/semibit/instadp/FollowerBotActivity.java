package in.semibit.instadp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import in.semibit.instadp.databinding.ActivityFollowerBotBinding;
import in.semibit.instadp.databinding.ActivityMainBinding;
import in.semibit.instadp.followerbot.FollowerBotService;
import in.semibit.instadp.followerbot.FollowerBotWidget;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_bot);
        binding = ActivityFollowerBotBinding.bind(findViewById(R.id.root));
        context = getApplicationContext();

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW}, 1234);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1112);

        } else {

        }
        binding.searchButton.setOnClickListener(c -> {
//            Intent intent = new Intent(context, FollowerBotService.class);
//            startForegroundService(intent);
            FollowerBotService s = new FollowerBotService();
            s.generateAlert(context);
        });
        binding.searchButton.callOnClick();

    }
}