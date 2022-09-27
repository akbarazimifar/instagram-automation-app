package in.semibit.media.followerbot.jobs;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.ratelimiter.RateLimiter;
import in.semibit.media.common.ratelimiter.SmoothRateLimiter;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.AccountsAPIHelper;
import in.semibit.media.followerbot.Constants;
import in.semibit.media.followerbot.FollowBotService;
import in.semibit.media.followerbot.FollowUserModel;
import in.semibit.media.followerbot.FollowUserState;
import in.semibit.media.followerbot.FollowerUtil;

public class FollowUsersViaAPIJob extends BatchJob<FollowUserModel, Boolean> {

    public static final String JOBNAME = "FollowUsersViaAPIJob";
    DatabaseHelper serverDb;
    static RateLimiter semaphore;
    AtomicInteger hourlySlots = new AtomicInteger(0);
    AccountsAPIHelper accountsAPIHelper;
    FollowerUtil followerUtil;

    public FollowUsersViaAPIJob(DatabaseHelper db,
                                FollowerUtil followerUtil) {
        super(LogsViewModel::addToLog);
        this.serverDb = db;

        double discreteRate = 3600.0; // hourly rate
        double permitsPerSecond = Constants.MAX_USERS_TO_BE_FOLLOWED_PER_HOUR / discreteRate;
        if (semaphore == null) {
            semaphore = new SmoothRateLimiter.SmoothBursty(RateLimiter.SleepingStopwatch.createFromSystemTimer(), discreteRate);
            semaphore.setRate(permitsPerSecond);
            LogsViewModel.addToLog("UnFollow Semaphores initialized with " + semaphore.getRate() + " permits/seconds");
        }
        this.followerUtil = followerUtil;
        accountsAPIHelper = new AccountsAPIHelper(followerUtil.getIgClient());
    }


    public boolean canIFollowNextUser() {
        if (FollowBotService.TEST_MODE) {
            LogsViewModel.addToLog("Skip semaphore slot check since in Test Mode");
            return true;
        }

        RateLimiter semaphore = FollowUsersViaAPIJob.semaphore;
        AtomicInteger slots = hourlySlots;
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
        getLogger().onStart(getJobName() + " JOB COMPLETED");
        return false;
    }


    @Override
    public GenericCompletableFuture<List<FollowUserModel>> getData() {
        GenericCompletableFuture<List<FollowUserModel>> completableFuture = followerUtil.getUserByFollowState(TableNames.FOLLOW_DATA, FollowUserState.TO_BE_FOLLOWED);
        GenericCompletableFuture<List<FollowUserModel>> completableFutureReturn = new GenericCompletableFuture<>();

        completableFuture
                .exceptionally(e -> {
                    e.printStackTrace();
                    return new ArrayList<>();
                }).thenAccept(newUsers -> {
                    if (newUsers == null) {
                        newUsers = new ArrayList<>();
                    }

                    //todo remove
                    newUsers = List.of(newUsers.get(0));

                    getLogger().onStart("Added " + newUsers.size() + " from firebase to follow queue. Total = " + newUsers.size());
                    completableFutureReturn.complete(newUsers);
                });
        return completableFutureReturn;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(FollowUserModel item) {
        getLogger().onStart("Follow User " + item.userName);
        GenericCompletableFuture<JobResult<Boolean>> onFollowCompletedFuture = new GenericCompletableFuture<>();

        CompletableFuture<String> followResult =
                accountsAPIHelper.follow(item, getAction());
        followResult.exceptionally(e -> {
            LogsViewModel.addToLog("Follow " + item.userName + " failed. " + e.getMessage());
            return null;
        }).thenAccept(followResultStr -> {
            if (followResultStr != null && !followResultStr.isEmpty()) {
                followerUtil.setUserAsFollowed(item).thenAccept(e -> {
                    onFollowCompletedFuture.complete(JobResult.success());
                });
            } else
                onFollowCompletedFuture.complete(JobResult.failed());
        });
        return onFollowCompletedFuture;
    }

    public String getAction() {
        return AccountsAPIHelper.ACTION_FOLLOW;
    }

    @Override
    public String getJobName() {
        return JOBNAME;
    }

    public static Instant nextScheduledTime(Instant prevIsntant) {
        int future = EzUtils.randomInt(40, 70);
        return prevIsntant.plus(future, FollowBotService.TEST_MODE ? ChronoUnit.SECONDS : ChronoUnit.MINUTES);
    }
}
