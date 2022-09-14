package in.semibit.media.followerbot.jobs;

import android.os.AsyncTask;
import android.util.Log;

import com.github.instagram4j.instagram4j.IGClient;

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
import in.semibit.media.common.igclientext.followers.FollowerInfoRequest;
import in.semibit.media.common.igclientext.followers.FollowerInfoResponse;
import in.semibit.media.common.igclientext.likes.LikeInfoRequest;
import in.semibit.media.common.igclientext.likes.LikeInfoResponse;
import in.semibit.media.common.igclientext.post.model.User;
import in.semibit.media.common.scheduler.BatchJob;
import in.semibit.media.common.scheduler.JobResult;
import in.semibit.media.followerbot.FollowBotService;
import in.semibit.media.followerbot.FollowUserModel;
import in.semibit.media.followerbot.FollowerUtil;
import in.semibit.media.followerbot.OffensiveWordFilter;

public class MarkUsersFromFollowersJob extends BatchJob<FollowUserModel,Boolean> {

    DatabaseHelper serverDb;
    FollowerUtil followerUtil;

    public MarkUsersFromFollowersJob(GenricDataCallback logger, DatabaseHelper serverDb, FollowerUtil igClient) {
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




    public void markUsersToFollowFromFollowers(String userName, GenricDataCallback cb,
                                               GenricDataCallback onUILog) {

        int maxFollowersToLoad = FollowBotService.TEST_MODE ? 15: 300;
        int maxFollowersToLoadPerBatch = FollowBotService.TEST_MODE ? 12: 100;
        AsyncTask.execute(() -> {
            IGClient client = followerUtil.getIgClient();
            onUILog.onStart("Started marking users from user " + userName);
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

                    com.github.instagram4j.instagram4j.models.user.User user = client.getActions().users().findByUsername(userName).get().getUser();
                    Long pk = user.getPk();
                    List<User> users = new ArrayList<>();
                    String nextMaxId = "";
                    boolean hasMoreFollowers = true;

                    while (users.size() < maxFollowersToLoad && hasMoreFollowers && nextMaxId != null) {

                        CompletableFuture<FollowerInfoResponse> completableFuture =
                                new FollowerInfoRequest(String.valueOf(pk), maxFollowersToLoadPerBatch, nextMaxId).execute(client);
                        FollowerInfoResponse followerInfoResponse = completableFuture.get();
                        if (followerInfoResponse.getFollowerModel() == null) {
                            cb.onStart("error . no followers found. is the profile public ?");
                            return;
                        }
                        hasMoreFollowers = followerInfoResponse.getFollowerModel().getBigList();
                        nextMaxId = followerInfoResponse.getFollowerModel().getNextMaxId();

                        List<String> followeIds = followersLists.stream().flatMap(e -> e.getFollowIds().stream()).collect(Collectors.toList());

                        List<User> localUsers = followerInfoResponse.getFollowers();
                        localUsers = localUsers.stream().filter(us -> {
                            boolean isNotAlreadyPresent = !followeIds.contains(String.valueOf(us.getPk()));
                            return isNotAlreadyPresent;
                        }).filter(new OffensiveWordFilter(onUILog)).collect(Collectors.toList());

                        users.addAll(localUsers);
                        onUILog.onStart("Retrieved new followers in batch = " + users.size());
                        Log.d("FollowerBot", "Total Follower Size = " + users.size());

                    }
                    onUILog.onStart("Total Followers retrieved " + users.size());
                    followerUtil.saveUsersToBeFollowed(users, followersLists, onUILog);

                    if (users.isEmpty()) {
                        cb.onStart("error . empty response");
                    } else {
                        cb.onStart("done saved " + users.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onStart("error " + e.getMessage());
                    onUILog.onStart(e.getMessage());
                }

            });

        });
    }

}
