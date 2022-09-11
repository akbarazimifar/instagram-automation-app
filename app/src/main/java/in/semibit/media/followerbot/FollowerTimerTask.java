package in.semibit.media.followerbot;

import android.widget.TextView;

import androidx.core.util.Pair;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;

public class FollowerTimerTask extends TimerTask {

    FollowerBotService followerBotService;
    GenricDataCallback uiLogger;
    Pair<TextView, AdvancedWebView> webViewPair;

    public FollowerTimerTask(FollowerBotService followerBotService, GenricDataCallback uiLogger, Pair<TextView, AdvancedWebView> webViewPair) {
        this.followerBotService = followerBotService;
        this.uiLogger = uiLogger;
        this.webViewPair = webViewPair;
    }

    @Override
    public void run() {
        if (!followerBotService.isRunning()) {
            uiLogger.onStart("Stopped scheduler");
            return;
        }
        uiLogger.onStart("Scheduled task started");
        boolean canIFollowUsers = followerBotService.canIFollowNextUser(false);
        if (canIFollowUsers) {
            followerBotService.getUsersToBeFollowed(uiLogger);
            followerBotService.startFollowingUsers(webViewPair.second, webViewPair.first,uiLogger);
        } else {
            int nextMins = EzUtils.randomInt(40, 80);
            Instant nextExecInstant = Instant.now().plus(nextMins, ChronoUnit.MINUTES);
            uiLogger.onStart("Next execution after " + nextMins + " mins at " + nextExecInstant.toString());
            Date nextExecution = Date.from(nextExecInstant);
            followerBotService.cancelFollowTimer();
            followerBotService.followTimer = new Timer();
            followerBotService.followTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    FollowerTimerTask.this.run();
                }
            }, nextExecution);
        }
    }
}
