package in.semibit.media.followerbot;

import android.widget.TextView;

import androidx.core.util.Pair;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;

public class UnFollowerTimerTask extends TimerTask {

    FollowerBotService followerBotService;
    GenricDataCallback uiLogger;
    Pair<TextView, AdvancedWebView> webViewPair;

    public UnFollowerTimerTask(FollowerBotService followerBotService, GenricDataCallback uiLogger, Pair<TextView, AdvancedWebView> webViewPair) {
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
        boolean canIFollowUsers = followerBotService.canIFollowNextUser(true, uiLogger);
        if (canIFollowUsers) {
            followerBotService.getUsersToBeUnFollowed(uiLogger, true);
            followerBotService.startUnFollowingUsers(webViewPair.second, webViewPair.first, uiLogger);
        }
        if (followerBotService.isRunning()) {
            int nextMins = EzUtils.randomInt(40, 80);
            Instant nextExecInstant = Instant.now().plus(nextMins, ChronoUnit.MINUTES);
            uiLogger.onStart("Next UnFollowerTimerTask execution after " + nextMins + " mins at " +  (ZonedDateTime.ofInstant(nextExecInstant, ZoneOffset.systemDefault())).toString());
            Date nextExecution = Date.from(nextExecInstant);
            followerBotService.cancelFollowTimer();
            followerBotService.unFollowTimer = new Timer();
            followerBotService.unFollowTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    UnFollowerTimerTask.this.run();
                }
            }, nextExecution);
        }
    }
}
