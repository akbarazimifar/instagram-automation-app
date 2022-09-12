package in.semibit.media.followerbot.jobs;

import android.os.Handler;
import android.widget.TextView;

import androidx.core.util.Pair;

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

public class FollowUsersJob extends BatchJob<FollowUserModel, Boolean> {

    Pair<TextView, AdvancedWebView> uiPair;
    DatabaseHelper db;
    Handler handler = new Handler();

    public FollowUsersJob(GenricDataCallback logger) {
        super(logger);
    }

    @Override
    public boolean onBatchCompleted(Map<FollowUserModel, JobResult<Boolean>> completedItems) {
        getLogger().onStart("JOB COMPLETED");
        return false;
    }

    @Override
    public GenericCompletableFuture<List<FollowUserModel>> getData() {
        FollowUserModel followUserModel = new FollowUserModel();
        followUserModel.id = "A";
        getLogger().onStart("JOB getData");

        GenericCompletableFuture dummy = new GenericCompletableFuture();
        handler.postDelayed(() -> {
            dummy.complete(Arrays.asList(followUserModel, followUserModel, followUserModel));
        }, 3000);
        return dummy;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(FollowUserModel item) {
        getLogger().onStart("JOB PROCESS " + item.getId());

        GenericCompletableFuture dummy = new GenericCompletableFuture();
        handler.postDelayed(() -> {
            dummy.complete("Complete");
        }, 3000);
        return dummy;
    }

}
