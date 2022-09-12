package in.semibit.media.followerbot.jobs;

import android.app.Activity;
import android.widget.TextView;

import androidx.core.util.Pair;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.GenericOperator;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.database.WhereClause;
import in.semibit.media.common.igclientext.FollowersList;
import in.semibit.media.common.ratelimiter.RateLimiter;
import in.semibit.media.common.ratelimiter.SmoothRateLimiter;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.Constants;
import in.semibit.media.followerbot.FollowUserModel;
import in.semibit.media.followerbot.FollowUserState;
import in.semibit.media.followerbot.FollowerBot;
import in.semibit.media.followerbot.FollowerUtil;

public class UnFollowUsersJob extends BatchJob<FollowUserModel, Boolean> {

    public static final String JOBNAME = "UnFollowUsersJob";
    Pair<TextView, AdvancedWebView> uiPair;
    DatabaseHelper serverDb;
    static RateLimiter unfollowSemaphore;
    FollowerUtil followerUtil;
    FollowerBot followerBot;
    AtomicInteger unfollowHourlySlots = new AtomicInteger(0);
    Activity activity;

    public UnFollowUsersJob(GenricDataCallback logger,
                            Pair<TextView, AdvancedWebView> uiPair,
                            DatabaseHelper db,
                            FollowerUtil followerUtil,
                            Activity activity) {
        super(logger);
        this.uiPair = uiPair;
        this.serverDb = db;
        this.activity = activity;

        double discreteRate = 3600.0; // hourly rate
        double permitsPerSecond = Constants.MAX_USERS_TO_BE_FOLLOWED_PER_HOUR / discreteRate;
        if (unfollowSemaphore == null) {
            unfollowSemaphore = new SmoothRateLimiter.SmoothBursty(RateLimiter.SleepingStopwatch.createFromSystemTimer(), discreteRate);
            unfollowSemaphore.setRate(permitsPerSecond);
            logger.onStart("UnFollow Semaphores initialized with " + unfollowSemaphore.getRate() + " permits/seconds");
        }
        this.followerUtil = followerUtil;
        followerBot = new FollowerBot(followerUtil.getIgClient(), uiPair.first::setText);
    }


    public boolean canIFollowNextUser() {
        RateLimiter semaphore = unfollowSemaphore;
        AtomicInteger slots = unfollowHourlySlots;
        int maxRate = Constants.MAX_USERS_TO_BE_UNFOLLOWED_PER_HOUR;

        boolean canIFollow = false;
        if (slots.get() > 0) {
            slots.getAndDecrement();
            canIFollow = true;
        } else {
            if (semaphore.tryAcquire(1)) {
                canIFollow = true;
                slots.set(slots.get() + (maxRate));
                getLogger().onStart("Slots refreshed");
            } else {
                getLogger().onStart("Cannot follow next user since no slots available. ");
            }
        }

        return canIFollow;
    }


    @Override
    public boolean isContinueToNext() {
        return super.isContinueToNext() && canIFollowNextUser();
    }

    @Override
    public boolean onBatchCompleted(Map<FollowUserModel, JobResult<Boolean>> completedItems) {
        getLogger().onStart(getJobName()+" JOB COMPLETED");
        return false;
    }

    @Override
    public GenericCompletableFuture<List<FollowUserModel>> getData() {
        GenericCompletableFuture<List<FollowUserModel>> completableFuture = new GenericCompletableFuture<>();
        GenericCompletableFuture<List<FollowersList>> onLoadedFollowMeta = serverDb.query(TableNames.FOLLOW_META, Collections.singletonList(WhereClause.of("id", GenericOperator.EQUAL, "to_be_follow")), FollowersList.class);
        onLoadedFollowMeta.exceptionally((e) -> {
            e.printStackTrace();
            return new ArrayList<>();
        }).thenAccept(followersLists -> {
            if (followersLists == null) {
                followersLists = new ArrayList<>();
            }
            if (followersLists.size() == 0) {
                followersLists.add(new FollowersList());
            }
            try {

                List<String> followeIds = followersLists.stream().flatMap(e -> e.getFollowIds().stream()).collect(Collectors.toList());


                long graceTime = System.currentTimeMillis();
                serverDb.query(TableNames.MY_FOLLOWING_DATA,
                        Arrays.asList(new WhereClause("tenant", GenericOperator.EQUAL, SemibitMediaApp.CURRENT_TENANT),
                                new WhereClause("waitTillFollowBackDate", GenericOperator.LESS_THAN, graceTime),
                                new WhereClause("waitTillFollowBackDate", GenericOperator.GREATER_THAN, 0),
                                new WhereClause("followUserState", GenericOperator.EQUAL, FollowUserState.FOLLOWED)),
                        FollowUserModel.class)
                        .exceptionally(e -> {
                            e.printStackTrace();
                            getLogger().onStart("Error fetching unfollow list " + e.getMessage());
                            return null;
                        })
                        .thenAccept(toBeBanished -> {
                            if (toBeBanished == null) {
                                completableFuture.complete(new ArrayList<>());
                                return;
                            }
                            List<FollowUserModel> newUsers = toBeBanished.stream().filter(
                                    user -> followeIds.contains(String.valueOf(user.getId()))
                            ).collect(Collectors.toList());
                            completableFuture.complete(newUsers);
                            getLogger().onStart("Added " + newUsers.size() + " from firebase to unfollow queue. Total = " + newUsers.size());
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return completableFuture;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(FollowUserModel item) {
        getLogger().onStart("Unfollow User " + item.userName);
        GenericCompletableFuture<JobResult<Boolean>> onFollowCompletedFuture =new  GenericCompletableFuture<>();

        followerBot.followUnfollow(item.userName,true,uiPair.second,activity,(str)->{})
        .thenAccept(result -> {
            if(result){
                onFollowCompletedFuture.complete(new JobResult<>(JobResult.SUCCESS_STATUS, true));
            }else {
                onFollowCompletedFuture.complete(new JobResult<>(JobResult.FAILED_STATUS, false));
            }
        });
        return onFollowCompletedFuture;
    }

    @Override
    public String getJobName() {
        return JOBNAME;
    }

    public static Instant nextScheduledTime(Instant prevIsntant) {
        return FollowUsersJob.nextScheduledTime(prevIsntant);
    }
}
