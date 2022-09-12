package in.semibit.media.followerbot;

import static in.semibit.media.followerbot.FollowerBotOrchestrator.TEST_MODE;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.instagram4j.instagram4j.IGClient;
import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.semibit.media.R;
import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricCallback;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.database.GenericOperator;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.database.WhereClause;
import in.semibit.media.common.igclientext.FollowersList;
import in.semibit.media.common.igclientext.followers.FollowerInfoRequest;
import in.semibit.media.common.igclientext.followers.FollowerInfoResponse;
import in.semibit.media.common.igclientext.followers.FollowingInfoRequest;
import in.semibit.media.common.igclientext.post.model.User;

public class FollowerUtil {

    private IGClient igClient;
    public DatabaseHelper serverDb;
    public GenricDataCallback logger;
    public Context context;
    public String  tenant;

    public FollowerUtil(IGClient igClient, DatabaseHelper serverDb, GenricDataCallback logger, Context context) {
        this.igClient = igClient;
        this.serverDb = serverDb;
        this.logger = logger;
        this.context = context;
        tenant = SemibitMediaApp.CURRENT_TENANT;
    }

    public static List<FollowUserModel> getUsersThatDontFollowMe(List<FollowUserModel> usersFollowingMe,
                                                                 List<FollowUserModel> usersIAmFollowing) {

        List<FollowUserModel> usersToBeUnFollowed = new ArrayList<>();
        List<FollowUserModel> mutualUsers = new ArrayList<>();

        for (FollowUserModel userModel : usersIAmFollowing) {
            if (usersFollowingMe.stream().anyMatch(uFm -> uFm.getId().equals(userModel.getId()))) {
                mutualUsers.add(userModel);
            } else {
                usersToBeUnFollowed.add(userModel);
            }
        }

        EzUtils.log("FollowerBot", "My Followers=" + usersFollowingMe.size() + "\n" +
                "I Follow=" + usersIAmFollowing.size() + "\n" +
                "Who didnt follow back=" + usersToBeUnFollowed.size() + "\n" +
                "Mutual=" + mutualUsers.size() + "\n");

        return usersToBeUnFollowed;
    }

    public static <T> List<List<T>> chopIntoParts(final List<T> ls, final int iParts) {
        final List<List<T>> lsParts = new ArrayList<List<T>>();
        final int iChunkSize = ls.size() / iParts;
        int iLeftOver = ls.size() % iParts;
        int iTake = iChunkSize;

        for (int i = 0, iT = ls.size(); i < iT; i += iTake) {
            if (iLeftOver > 0) {
                iLeftOver--;

                iTake = iChunkSize + 1;
            } else {
                iTake = iChunkSize;
            }

            lsParts.add(new ArrayList<T>(ls.subList(i, Math.min(iT, i + iTake))));
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
                IGClient client = igClient;
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
                    GenericCompletableFuture<Void> onSave = serverDb.save(
                            isIncomingConnection ? TableNames.MY_FOLLOWING_DATA : TableNames.MY_FOLLOWERS_DATA
                            , new ArrayList<>(newUsers));
                    onSave.thenAccept(v -> {
                        usersResultFuture.complete(newUsers);
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
                        serverDb.query(TableNames.FOLLOW_DATA, conditions, FollowUserModel.class)
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
                    serverDb.save(TableNames.COUNTER, new FollowerCounter(TableNames.withTablePrefix("following_count"), users.size()));
                } else {
                    serverDb.save(TableNames.COUNTER, new FollowerCounter(TableNames.withTablePrefix("follower_count"), users.size()));
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


    public void saveUsersToBeFollowed(List<User> users, List<FollowersList>
            followeIdList, GenricDataCallback onUILog) {

        if (TEST_MODE) {
            logger.onStart("TEST MODE : skip saveUsersToBeFollowed");
            return;
        }

        List<FollowUserModel> newUsers = users.stream()
                .map(FollowUserModel::fromUserToBeFollowed).collect(Collectors.toList());

        try {
            FollowersList addTO = followeIdList.get(EzUtils.randomInt(0, followeIdList.size() - 1));
            addTO.getFollowIds().addAll(newUsers.stream().map(u -> u.id).collect(Collectors.toList()));
            GenericCompletableFuture<Void> onSave = serverDb.save(TableNames.FOLLOW_DATA, new ArrayList<>(newUsers));
            onSave.thenAccept(v -> {
                onUILog.onStart("Completed marking users");
                String newUserNames = newUsers.stream().map(u -> u.userName).collect(Collectors.joining("\n"));
                onUILog.onStart(newUserNames);
                onUILog.onStart("Saved new followers to process " + newUsers.size());
            });
            serverDb.save(TableNames.FOLLOW_META, addTO).thenAccept(v -> {
                onUILog.onStart("Saved meta of new followers to " + addTO.getId() + " partition " + newUsers.size());
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.onStart("Error Saving" + e.getMessage());
        }
    }


    public IGClient getIgClient() {
        if (igClient == null)
            igClient = Insta4jClient.getClient(context.getString(R.string.username),
                    context.getString(R.string.password), (s) -> {
                    });
        return igClient;
    }


    public GenericCompletableFuture<Boolean> followSingleUser(FollowerBot followerBot, boolean isDoUnfollow, FollowUserModel user, AdvancedWebView webView, Activity context, GenricDataCallback uiLogger) {
        String action = isDoUnfollow ? "Unfollow" : "Follow";
        GenericCompletableFuture<Boolean> followResult = new GenericCompletableFuture<>();


        uiLogger.onStart("Trying to " + action + " " + user.userName);
        followerBot.followUnfollow(user.userName, isDoUnfollow, webView, context, s -> {
            Log.e("FollowerBot", "Follow Completed");
            if (s.contains("unabletocomplete")) {
                uiLogger.onStart("Error connecting to " + user.userName + ". Timeout or some other issue");
                followResult.complete(false);
                return;
            }
            (isDoUnfollow ? setUserAsUnFollowed(user) : setUserAsFollowed(user)).exceptionally(e -> {
                uiLogger.onStart("Error saving after " + action + " " + user.userName + " " + e.getMessage());
                followResult.complete(false);
                return null;
            }).thenAccept(e -> {
                if (!followResult.isDone())
                    followResult.complete(true);
                uiLogger.onStart("Completed " + action + " " + user.userName+" "+(followResult.get()));
            });
        });
        return followResult;
    }

    public GenericCompletableFuture<Void> setUserAsUnFollowed(FollowUserModel userModel) {
        Map map = new HashMap<>();
        map.put("followUserState", FollowUserState.UNFOLLOWED);
        map.put("id", userModel.getId());
        map.put("unfollowDate", System.currentTimeMillis());
        if (TEST_MODE) {
            logger.onStart("TEST MODE : skip setUserAsUnFollowed");
            return GenericCompletableFuture.genericCompletedFuture(null);
        }
        serverDb.updateOne((TableNames.MY_FOLLOWING_DATA), map);
        return serverDb.updateOne((TableNames.FOLLOW_DATA), map);
    }


    public GenericCompletableFuture<Void> setUserAsFollowed(FollowUserModel userModel) {
        userModel.followUserState = FollowUserState.FOLLOWED;
        userModel.followDate = System.currentTimeMillis();
        Instant unFollowOn = Instant.now().plus(2, ChronoUnit.DAYS);
        userModel.waitTillFollowBackDate = unFollowOn.toEpochMilli();
        logger.onStart("Gonna Unfollow " + userModel.userName + " after " + (ZonedDateTime.ofInstant(unFollowOn, ZoneOffset.systemDefault())).toString());

        if (TEST_MODE) {
            return GenericCompletableFuture.genericCompletedFuture(null);
        }
        return serverDb.save((TableNames.FOLLOW_DATA), userModel);
    }

    public GenericCompletableFuture<List<FollowUserModel>> getUserByFollowState(String table, FollowUserState followUserState) {
        List<WhereClause> where = new ArrayList<>();
        if (followUserState != null)
            where.add(new WhereClause<FollowUserState>("followUserState", GenericOperator.EQUAL, followUserState));
        where.add(new WhereClause<String>("tenant", GenericOperator.EQUAL, tenant));
        where.add(new WhereClause<Integer>(null, GenericOperator.LIMIT, Constants.MAX_USERS_TO_BE_FOLLOWED_PER_HOUR));
        return serverDb.query(table, where, FollowUserModel.class);
    }

}
