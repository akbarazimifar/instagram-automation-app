package in.semibit.media.followerbot.jobs;

import com.github.instagram4j.instagram4j.IGClient;
import com.semibit.ezandroidutils.EzUtils;

import java.util.List;
import java.util.Map;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.igclientext.StringIGResponse;
import in.semibit.media.common.igclientext.likes.LikeUnlikePostRequest;
import in.semibit.media.common.igclientext.likes.UserTimelineRequest;
import in.semibit.media.common.igclientext.post.model.PostItem;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.FollowUserModel;

public class LikeUserPosts extends BatchJob<PostItem, Boolean> {

    FollowUserModel targetUser;
    IGClient client;

    public LikeUserPosts(GenricDataCallback logger, IGClient client, FollowUserModel targetUser) {
        super(logger);
        this.client = client;
        this.targetUser = targetUser;
    }

    @Override
    public boolean onBatchCompleted(Map<PostItem, JobResult<Boolean>> completedItems) {
        return false;
    }

    @Override
    public GenericCompletableFuture<List<PostItem>> getData() {
        UserTimelineRequest req = new UserTimelineRequest(client);
        int countPosts = EzUtils.randomInt(2,5);
        GenericCompletableFuture<List<PostItem>> data = req.getPosts(targetUser,countPosts);
        return data;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(PostItem item) {
        GenericCompletableFuture<JobResult<Boolean>> future = new GenericCompletableFuture<JobResult<Boolean>>();

        LikeUnlikePostRequest request = new LikeUnlikePostRequest(item.getId(), "like");
        StringIGResponse response = request.execute(client).join();

        try {
            Thread.sleep(EzUtils.randomInt(1000,10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response.getStatusCode() == 200) {
            LogsViewModel.addToLog("Like post "+item.getCode()+" OK");
            future.complete(JobResult.success());
        } else {
            LogsViewModel.addToLog("Like post "+item.getCode()+" Fail : "+response.getBody());
            future.complete(JobResult.failed());
        }
        return future;
    }
}
