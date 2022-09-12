package in.semibit.media.followerbot;

import android.os.AsyncTask;
import android.util.Log;

import com.github.instagram4j.instagram4j.IGClient;
import com.semibit.ezandroidutils.EzUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.GenricCallback;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.GenericOperator;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.database.WhereClause;
import in.semibit.media.common.igclientext.followers.FollowerInfoRequest;
import in.semibit.media.common.igclientext.followers.FollowerInfoResponse;
import in.semibit.media.common.igclientext.followers.FollowingInfoRequest;
import in.semibit.media.common.igclientext.post.model.User;

public class FollowerUtil {

    FollowerBotService followerBotService;

    public FollowerUtil(FollowerBotService followerBotService) {
        this.followerBotService = followerBotService;
    }

    public static List<FollowUserModel> getUsersThatDontFollowMe(List<FollowUserModel> usersFollowingMe,
                                                                 List<FollowUserModel> usersIAmFollowing){

        List<FollowUserModel> usersToBeUnFollowed = new ArrayList<>();
        List<FollowUserModel> mutualUsers = new ArrayList<>();

        for(FollowUserModel userModel:usersIAmFollowing){
            if(usersFollowingMe.stream().anyMatch(uFm-> uFm.getId().equals(userModel.getId()))){
                mutualUsers.add(userModel);
            }
            else {
                usersToBeUnFollowed.add(userModel);
            }
        }

        EzUtils.log("FollowerBot","My Followers="+usersFollowingMe.size()+"\n"+
                "I Follow="+usersIAmFollowing.size()+"\n"+
                "Who didnt follow back="+usersToBeUnFollowed.size()+"\n"+
                "Mutual="+mutualUsers.size()+"\n");

        return usersToBeUnFollowed;
    }

    public static <T>List<List<T>> chopIntoParts( final List<T> ls, final int iParts )
    {
        final List<List<T>> lsParts = new ArrayList<List<T>>();
        final int iChunkSize = ls.size() / iParts;
        int iLeftOver = ls.size() % iParts;
        int iTake = iChunkSize;

        for( int i = 0, iT = ls.size(); i < iT; i += iTake )
        {
            if( iLeftOver > 0 )
            {
                iLeftOver--;

                iTake = iChunkSize + 1;
            }
            else
            {
                iTake = iChunkSize;
            }

            lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
        }

        return lsParts;
    }


    // DONT USE THIS FOR MARKING FOLLOWERS
    // because of this login
    /*
                            .peek(usr -> {
                            if (isFollowingRequest) {
                                usr.isUserFollowingMeState = FollowUserState.UNKNOWN;
                                usr.followUserState = FollowUserState.FOLLOWED;
                            } else {
                                usr.isUserFollowingMeState = FollowUserState.FOLLOWED;
                                usr.followUserState = FollowUserState.UNKNOWN;
                            }
                        })
     */
    public GenericCompletableFuture<List<FollowUserModel>> syncConnectionsForUserToFirebase(String userName, boolean isIncomingConnection, GenricDataCallback cb, GenricDataCallback onUILog) {

        GenericCompletableFuture<List<FollowUserModel>> usersResultFuture = new GenericCompletableFuture<>();
        AsyncTask.execute(() -> {
            try {
                IGClient client = followerBotService.getIgClient();
                String connections = isIncomingConnection ? "Followings" : "Followers";
                onUILog.onStart("Syncing " + connections + " from IG");

                com.github.instagram4j.instagram4j.models.user.User user = client.getActions().users().findByUsername(userName).get().getUser();
                Long pk = user.getPk();
                List<User> users = new ArrayList<>();
                String nextMaxId = "";
                boolean hasMoreFollowers = true;

                while (hasMoreFollowers && nextMaxId != null) {

                    CompletableFuture<FollowerInfoResponse> completableFuture =
                            (isIncomingConnection ? new FollowingInfoRequest(String.valueOf(pk), 100, nextMaxId)
                                    : new FollowerInfoRequest(String.valueOf(pk), 100, nextMaxId)).execute(client);
                    FollowerInfoResponse followerInfoResponse = completableFuture.get();
                    if (followerInfoResponse.getFollowerModel() == null) {
                        cb.onStart("error . no followers found. is this profile public ?");
                        return;
                    }
                    hasMoreFollowers = followerInfoResponse.getFollowerModel().getBigList();
                    nextMaxId = followerInfoResponse.getFollowerModel().getNextMaxId();

                    users.addAll(followerInfoResponse.getFollowers());
                    onUILog.onStart("Retrieved new " + connections + " in batch = " + users.size());
                    Log.d("FollowerBot", "Total " + connections + " Size = " + users.size());
                }
                onUILog.onStart("Total " + connections + " retrieved " + users.size());

                List<FollowUserModel> newUsers = users.stream()
                        .map(FollowUserModel::fromUserToBeFollowed)
                        .peek(usr -> {
                            if (isIncomingConnection) {
                                usr.isUserFollowingMeState = FollowUserState.UNKNOWN;
                                usr.followUserState = FollowUserState.FOLLOWED;
                            } else {
                                usr.isUserFollowingMeState = FollowUserState.FOLLOWED;
                                usr.followUserState = FollowUserState.UNKNOWN;
                            }
                        })
                        .collect(Collectors.toList());

                GenricCallback continueToSaveCB = () -> {
                    GenericCompletableFuture<Void> onSave = followerBotService.serverDb.save(
                            isIncomingConnection ? TableNames.MY_FOLLOWING_DATA : TableNames.MY_FOLLOWERS_DATA
                            , new ArrayList<>(newUsers));
                    onSave.thenAccept(v -> {
                        usersResultFuture.complete(newUsers);
                        followerBotService.getUsersToBeUnFollowed(onUILog, true);
                        onUILog.onStart("Completed syncing IG " + connections + " " + newUsers.size());
                    });
                };

                // In case of updating followings
                // we need to make sure we update the grace period
                if (isIncomingConnection) {

                    CompletableFuture<List<FollowUserModel>> batchReadAll = new CompletableFuture<>();
                    List<List<FollowUserModel>> batches = FollowerUtil.chopIntoParts(newUsers, newUsers.size() / 9);
                    List<FollowUserModel> userListIntersectionFollowedAutomatically = new ArrayList<>();
                    AtomicInteger completedBatches = new AtomicInteger(0);


                    for (final List<FollowUserModel> singleBatch : batches) {
                        List<WhereClause> conditions = new ArrayList<>();
                        List<String> actualInstagramIds = singleBatch.stream().map(FollowUserModel::getId).collect(Collectors.toList());
                        conditions.add(new WhereClause("id", GenericOperator.IN, actualInstagramIds));
                        conditions.add(new WhereClause("tenant", GenericOperator.EQUAL, SemibitMediaApp.CURRENT_TENANT));
                        followerBotService.serverDb.query(TableNames.FOLLOW_DATA, conditions, FollowUserModel.class)
                                .exceptionally(e -> {
                                    e.printStackTrace();
                                    onUILog.onStart("Error in sync" + e.getMessage());
                                    return new ArrayList<>();
                                })
                                .thenAccept(chunk -> {

                                    userListIntersectionFollowedAutomatically.addAll(chunk);
                                    onUILog.onStart("Retrieved chunk " + chunk.size() + " (" + (completedBatches.incrementAndGet()) + "/" + batches.size() + ") from input of size " + singleBatch.size());
                                    if (completedBatches.get() == batches.size()) {
                                        batchReadAll.complete(userListIntersectionFollowedAutomatically);
                                    }

                                });
                    }

                    batchReadAll.thenAccept(all -> {
                        completedBatches.set(0);
                        newUsers.forEach(newUser -> {
                            Optional<FollowUserModel> fromAuto =
                                    userListIntersectionFollowedAutomatically
                                            .stream().filter(au -> au.getId().equals(newUser.getId()))
                                            .findAny();
                            if (fromAuto.isPresent()) {
                                completedBatches.incrementAndGet();
                                newUser.followUserState = fromAuto.get().followUserState;
                                newUser.waitTillFollowBackDate = fromAuto.get().waitTillFollowBackDate;
                                newUser.followDate = fromAuto.get().followDate;
                            }
                        });
                        onUILog.onStart("Completed correlating actual and automated following. Users accepted my follow request = " + completedBatches.get());
                        continueToSaveCB.onStart();
                    });

                } else {
                    continueToSaveCB.onStart();
                }

                if (isIncomingConnection) {
                    followerBotService.serverDb.save(TableNames.COUNTER, new FollowerCounter(TableNames.withTablePrefix("following_count"), users.size()));
                } else {
                    followerBotService.serverDb.save(TableNames.COUNTER, new FollowerCounter(TableNames.withTablePrefix("follower_count"), users.size()));
                }

                if (users.isEmpty()) {
                    cb.onStart("error . empty response");
                } else {
                    cb.onStart("done saved " + users.size());
                }

            } catch (Exception e) {
                e.printStackTrace();
                cb.onStart("error " + e.getMessage());
                onUILog.onStart(e.getMessage());
                usersResultFuture.completeExceptionally(e);
            }
        });
        return usersResultFuture;
    }

}
