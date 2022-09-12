package in.semibit.media.followerbot.jobs;

import android.widget.TextView;

import androidx.core.util.Pair;

import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.FollowUserModel;

public class UnFollowUsersJob extends BatchJob<FollowUserModel, Boolean> {

    public static final String JOBNAME = "UnFollowUsersJob";
    Pair<TextView, AdvancedWebView> uiPair;
    DatabaseHelper db;

    public UnFollowUsersJob(GenricDataCallback logger, Pair<TextView, AdvancedWebView> uiPair, DatabaseHelper db) {
        super(logger);
        this.uiPair = uiPair;
        this.db = db;
    }

    @Override
    public boolean onBatchCompleted(Map<FollowUserModel, JobResult<Boolean>> completedItems) {
        getLogger().onStart("JOB COMPLETED");
        return false;
    }

    @Override
    public GenericCompletableFuture<List<FollowUserModel>> getData() {


        GenericCompletableFuture dummy = new GenericCompletableFuture();
        runAsync(() -> {
            dummy.complete(Arrays.asList(FollowUserModel.random(), FollowUserModel.random(), FollowUserModel.random()));
        }, 3000);
        return dummy;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(FollowUserModel item) {
        getLogger().onStart("JOB PROCESS " + item.getId());

        GenericCompletableFuture dummy = new GenericCompletableFuture();
        runAsync(() -> {
            dummy.complete(new JobResult<>(JobResult.SUCCESS_STATUS,"Complete"));
        }, 3000);
        return dummy;
    }

    @Override
    public String getJobName() {
        return JOBNAME;
    }

    public static Instant nextScheduledTime(Instant prevIsntant){
        int future = 10;EzUtils.randomInt(5,9);
        return prevIsntant.plus(future, ChronoUnit.SECONDS);
    }
}
