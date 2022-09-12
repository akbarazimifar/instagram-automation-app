package in.semibit.media.followerbot;

import android.content.Intent;

import com.semibit.ezandroidutils.EzUtils;

import in.semibit.media.common.BGService;
import in.semibit.media.followerbot.jobs.FollowUsersJob;

public class FollowerBotForegroundService extends BGService {
    FollowUsersJob followUsersJob;
    @Override
    public void work(Intent entry) {
//     FollowerBotOrchestrator.triggerBroadCast(this, FollowerBotOrchestrator.ACTION_BOT_START);
        followUsersJob = new FollowUsersJob(s -> {
            EzUtils.log("FollowerBot BatchJob" + s);
            updateNotification(s, true);
        }){

        };
        followUsersJob.start();
    }

    @Override
    public void stopWork(Intent intent) {
        if(followUsersJob!=null){
            followUsersJob.stop(true);
        }
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
    public String getServiceName() {
        return "Follower Service";
    }

    @Override
    protected Class<?> getOverriddenClass() {
        return FollowerBotForegroundService.class;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        FollowerBotOrchestrator.triggerBroadCast(this, FollowerBotOrchestrator.ACTION_BOT_STOP);
    }
}
