package in.semibit.media.followerbot;

import android.content.Intent;

import in.semibit.media.common.BGService;

public class FollowerBotForegroundService extends BGService {
    @Override
    public void work(Intent entry) {
     FollowerBotOrchestrator.triggerBroadCast(this, FollowerBotOrchestrator.ACTION_BOT_START);
    }

    @Override
    public int getNotificationId() {
        return 8291;
    }

    @Override
    public String getActionStopId() {
        return "FollowerBotForegroundService_STOP";
    }

    @Override
    protected Class<?> getOverriddenClass() {
        return FollowerBotForegroundService.class;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FollowerBotOrchestrator.triggerBroadCast(this, FollowerBotOrchestrator.ACTION_BOT_STOP);
    }
}
