package in.semibit.media.followerbot.jobs;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.GenericOperator;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.database.WhereClause;
import in.semibit.media.common.igclientext.FollowersList;
import in.semibit.media.common.igclientext.likes.LikeInfoRequest;
import in.semibit.media.common.igclientext.likes.LikeInfoResponse;
import in.semibit.media.common.igclientext.post.model.User;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.FollowUserModel;
import in.semibit.media.followerbot.FollowerUtil;
import in.semibit.media.followerbot.OffensiveWordFilter;

public class MarkUserFromPostJob extends BatchJob<FollowUserModel,Boolean> {

    DatabaseHelper serverDb;
    FollowerUtil followerUtil;

    public MarkUserFromPostJob(GenricDataCallback logger, DatabaseHelper serverDb, FollowerUtil igClient) {
        super(logger);
        this.serverDb = serverDb;
        this.followerUtil = igClient;
    }

    @Override
    public boolean onBatchCompleted(Map<FollowUserModel, JobResult<Boolean>> completedItems) {
        return false;
    }

    @Override
    public GenericCompletableFuture<List<FollowUserModel>> getData() {
        return null;
    }

    @Override
    public GenericCompletableFuture<JobResult<Boolean>> processItem(FollowUserModel item) {
        return null;
    }


    public void markUsersToFollowFromPostLikers(String shortCode, GenricDataCallback cb, GenricDataCallback onUILog) {

        AsyncTask.execute(() -> {
            onUILog.onStart("Started marking users from post " + shortCode);
            CompletableFuture<LikeInfoResponse> completableFuture = new LikeInfoRequest(shortCode).execute(followerUtil.getIgClient());
            GenericCompletableFuture<List<FollowersList>> onLoadedFollowMeta = serverDb.query(TableNames.FOLLOW_META, Collections.singletonList(WhereClause.of("type", GenericOperator.EQUAL, "to_be_follow")), FollowersList.class);
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

                    LikeInfoResponse postInfoResponse = completableFuture.get();
                    List<User> users = postInfoResponse.getLikers();
                    users = users.stream().filter(us -> {
                        boolean isNotAlreadyPresent = !followeIds.contains(String.valueOf(us.getPk()));
                        return isNotAlreadyPresent;
                    }).filter(new OffensiveWordFilter(onUILog)).collect(Collectors.toList());

                    cb.onStart("done saved " + users.size());
                    followerUtil.saveUsersToBeFollowed(users, followersLists, onUILog);
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onStart("error " + e.getMessage());

                }
            });


        });
    }

}
