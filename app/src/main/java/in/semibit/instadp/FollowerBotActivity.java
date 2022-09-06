package in.semibit.instadp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import in.semibit.instadp.databinding.ActivityFollowerBotBinding;
import in.semibit.instadp.databinding.ActivityMainBinding;
import in.semibit.instadp.followerbot.FollowerBotService;

public class FollowerBotActivity extends AppCompatActivity {

    ActivityFollowerBotBinding binding;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_bot);
        binding = ActivityFollowerBotBinding.bind(findViewById(R.id.root));
        context = getApplicationContext();


        binding.searchButton.setOnClickListener(c->{
            Intent intent = new Intent(context, FollowerBotService.class);
            startForegroundService(intent);
        });

    }
}