package in.semibit.instadp.followerbot;

import android.content.Intent;

import in.semibit.instadp.common.BGService;

public class FollowerBotService extends BGService {

    @Override
    public void work(Intent intent) {

    }

    @Override
    public int getNotificationId() {
        return 13248;
    }

    @Override
    public String getActionStopId() {
        return "6436";
    }

    @Override
    protected Class<?> getOverriddenClass() {
        return getClass();
    }
}
