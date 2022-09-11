package in.semibit.media.followerbot;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.util.Pair;

import com.github.instagram4j.instagram4j.IGClient;
import com.google.firebase.firestore.Source;
import com.semibit.ezandroidutils.EzUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.semibit.media.FollowerBotActivity;
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
import in.semibit.media.common.igclientext.likes.LikeInfoRequest;
import in.semibit.media.common.igclientext.likes.LikeInfoResponse;
import in.semibit.media.common.igclientext.post.model.User;
import in.semibit.media.common.ratelimiter.RateLimiter;
import in.semibit.media.common.ratelimiter.RateLimiter.SleepingStopwatch;
import in.semibit.media.common.ratelimiter.SmoothRateLimiter;

public class FollowerBotService {

    public static final int MAX_USERS_TO_BE_FOLLOWED_PER_HOUR = 150 / 24;
    public static final int MAX_USERS_TO_BE_UNFOLLOWED_PER_HOUR = 150 / 24;

    Activity context;
    DatabaseHelper serverDb;
    DatabaseHelper localDb;
    List<FollowUserModel> userToFollow;
    public View followWidget;
    public View unFollowWidget;
    String tenant;

    Timer followTimer, unFollowTimer;
    Queue<FollowUserModel> toBeFollowedQueue = new ConcurrentLinkedDeque<>();
    Queue<FollowUserModel> toBeUnFollowedQueue = new ConcurrentLinkedDeque<>();
    RateLimiter followSemaphore;
    RateLimiter unfollowSemaphore;
    boolean isRunning = false;


    public FollowerBotService(Activity context) {
        this.context = context;
        serverDb = new DatabaseHelper(Source.SERVER);
        localDb = new DatabaseHelper(Source.CACHE);
        tenant = "semibitmedia";

        double discreteRate = 3600.0; // hourly rate
        double permitsPerSecond = MAX_USERS_TO_BE_FOLLOWED_PER_HOUR / discreteRate;

        followSemaphore = new SmoothRateLimiter.SmoothBursty(SleepingStopwatch.createFromSystemTimer(), discreteRate);
        followSemaphore.setRate(permitsPerSecond);

        unfollowSemaphore = new SmoothRateLimiter.SmoothBursty(SleepingStopwatch.createFromSystemTimer(), discreteRate);
        unfollowSemaphore.setRate(permitsPerSecond);

        logger.onStart("Follow Semaphores initialized with " + followSemaphore.getRate() + " permits/seconds");
        logger.onStart("UnFollow Semaphores initialized with " + unfollowSemaphore.getRate() + " permits/seconds");
    }

    private IGClient getIgClient() {
        return Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), null);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancelFollowTimer() {
        if (followTimer != null) {
            try {
                followTimer.cancel();
                followTimer.purge();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void cancelUnFollowTimer() {
        if (unFollowTimer != null) {
            try {
                unFollowTimer.cancel();
                unFollowTimer.purge();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void kill(GenricDataCallback logger) {
        try {
            isRunning = false;
            cancelFollowTimer();
            cancelUnFollowTimer();
            WindowManager mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            if (followWidget != null) {
                try {
                    mWindowManager.removeView(followWidget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (unFollowWidget != null) {
                try {
                    mWindowManager.removeView(unFollowWidget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.onStart("Killed FollowerBotService");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void cronStart(Pair<TextView, AdvancedWebView> followWebView,
                          Pair<TextView, AdvancedWebView> unFollowWebView,
                          GenricDataCallback uiLogger) {
        cancelFollowTimer();
        cancelUnFollowTimer();
        isRunning = true;
        GenricCallback onFollowStart = new GenricCallback() {
            @Override
            public void onStart() {
                followTimer = new Timer();
                TimerTask followerTimerTask = new FollowerTimerTask(FollowerBotService.this, uiLogger, followWebView);
                followTimer.schedule(followerTimerTask, 0);
            }
        };

        GenricCallback onUnFollowStart = () -> {
            unFollowTimer = new Timer();
            TimerTask unFollowerTimerTask = new UnFollowerTimerTask(FollowerBotService.this, uiLogger, unFollowWebView);
            unFollowTimer.schedule(unFollowerTimerTask, 0);
        };

//        followWebView.second.setVisibility(View.GONE);
        onFollowStart.onStart();
        onUnFollowStart.onStart();

    }

    public GenericCompletableFuture<List<FollowUserModel>> getUserByFollowState(String table, FollowUserState followUserState) {
        List<WhereClause> where = new ArrayList<>();
        if (followUserState != null)
            where.add(new WhereClause<FollowUserState>("followUserState", GenericOperator.EQUAL, followUserState));
        where.add(new WhereClause<String>("tenant", GenericOperator.EQUAL, tenant));
        where.add(new WhereClause<Integer>(null, GenericOperator.LIMIT, MAX_USERS_TO_BE_FOLLOWED_PER_HOUR));
        return serverDb.query(TableNames.FOLLOW_DATA, where, FollowUserModel.class);
    }


    public View setAndGetView(View view, String viewType) {
        if (viewType.equals("follow")) {
            if (view != null) {
                followWidget = view;
            }
            return followWidget;
        } else if (viewType.equals("unfollow")) {
            if (view != null) {
                unFollowWidget = view;
            }
            return unFollowWidget;
        }
        return null;
    }

    public Pair<TextView, AdvancedWebView> generateAlert(final Activity context, String viewType) {

        View followWidget = setAndGetView(null, viewType);

        int layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,   // REMOVE FLAG_NOT_FOCUSABLE
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        WindowManager mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (followWidget != null) {
            try {
                mWindowManager.removeView(followWidget);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        followWidget = LayoutInflater.from(context).inflate(R.layout.follower_bot, null);
        mWindowManager.addView(followWidget, params);


        final TextView label = followWidget.findViewById(R.id.label);
        final AdvancedWebView webView = followWidget.findViewById(R.id.webView);


        View finalWidget = followWidget;
        followWidget.setOnLongClickListener((v) -> {
            mWindowManager.removeView(finalWidget);
            return true;
        });
        followWidget.setOnClickListener(c -> {
            Intent intent = new Intent(context, FollowerBotActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        int MAX_X_MOVE = 10;
        int MAX_Y_MOVE = 10;
        followWidget.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            float mX = params.x;
            float mY = params.y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("AD", "Action E" + event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("AD", "Action Down");
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        if (Math.abs(event.getX() - mX) < MAX_X_MOVE || Math.abs(event.getY() - mY) < MAX_Y_MOVE) {
                            v.performClick();
                        }
                        mX = event.getX();
                        mY = event.getY();

                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d("AD", "Action Up");
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        if (Xdiff < 10 && Ydiff < 10) {
//                            if (isViewCollapsed()) {
//                                collapsedView.setVisibility(View.GONE);
//                                expandedView.setVisibility(View.VISIBLE);
//                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("AD", "Action Move");
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(finalWidget, params);
                        return true;
                }
                return false;
            }
        });
        setAndGetView(followWidget, viewType);
        return Pair.create(label, webView);

    }


    AtomicInteger followHourlySlots = new AtomicInteger(0);
    AtomicInteger unfollowHourlySlots = new AtomicInteger(0);

    public boolean canIFollowNextUser(boolean isDoUnfollow) {
        return canIFollowNextUser(isDoUnfollow, logger);
    }

    public boolean canIFollowNextUser(boolean isDoUnfollow, GenricDataCallback logger) {

        RateLimiter semaphore = isDoUnfollow ? unfollowSemaphore : followSemaphore;
        Queue<FollowUserModel> queue = isDoUnfollow ? toBeUnFollowedQueue : toBeFollowedQueue;
        AtomicInteger slots = isDoUnfollow ? unfollowHourlySlots : followHourlySlots;
        int maxRate = isDoUnfollow ? MAX_USERS_TO_BE_UNFOLLOWED_PER_HOUR : MAX_USERS_TO_BE_FOLLOWED_PER_HOUR;

        boolean canIFollow = false;
        if (slots.get() > 0) {
            slots.getAndDecrement();
            canIFollow = true;
        } else {
            if (semaphore.tryAcquire(1)) {
                canIFollow = true;
                slots.set(slots.get() + (maxRate));
                logger.onStart("Slots refreshed");
            } else {
                logger.onStart("Cannot follow next user since no slots available. ");
            }
        }
        if (queue.isEmpty()) {
            canIFollow = false;
            logger.onStart("Cannot follow next user since queue is empty.");
        }
        return canIFollow;
    }

    ////////////// FOLLOW /////////////////////////
    public FollowUserModel getNextUserToFollow() {
        return toBeFollowedQueue.remove();
    }

    public CompletableFuture<Void> setUserAsFollowed(FollowUserModel userModel) {
        userModel.followUserState = FollowUserState.FOLLOWED;
        userModel.followDate = System.currentTimeMillis();
        Instant unFollowOn = Instant.now().plus(2, ChronoUnit.DAYS);
        userModel.waitTillFollowBackDate = unFollowOn.toEpochMilli();
        logger.onStart("Gonna Unfollow " + userModel.userName + " after " + (ZonedDateTime.ofInstant(unFollowOn, ZoneOffset.systemDefault())).toString());
        return serverDb.save((TableNames.FOLLOW_DATA), userModel);
    }

    public void startFollowingUsers(AdvancedWebView webView, TextView label, GenricDataCallback uiLogger) {
        AsyncTask.execute(() -> {
            final IGClient client = getIgClient();
            FollowerBot followerBot = new FollowerBot(client, s -> {
                Log.e("FollowerBot", "" + s);
                try {
                    label.setText(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            context.runOnUiThread(() -> {
                followSingleUser(followerBot, false, getNextUserToFollow(), webView, context, uiLogger);
            });
        });
    }

    public void followSingleUser(FollowerBot followerBot, boolean isDoUnfollow, FollowUserModel user, AdvancedWebView webView, Activity context, GenricDataCallback uiLogger) {
        String action = isDoUnfollow ? "Unfollow" : "Follow";
        if (user == null || !canIFollowNextUser(isDoUnfollow) || !isRunning()) {
            uiLogger.onStart("Paused " + action + "ing users");
            return;
        }
        uiLogger.onStart("Trying to " + action + " " + user.userName);
        followerBot.followUnfollow(user.userName, isDoUnfollow, webView, context, s -> {
            Log.e("FollowerBot", "Follow Completed");
            (isDoUnfollow ? setUserAsUnFollowed(user) : setUserAsFollowed(user)).exceptionally(e -> {
                uiLogger.onStart("Error saving after " + action + " " + user.userName + " " + e.getMessage());
                return null;
            }).thenAccept(e -> {
                uiLogger.onStart("Successfully " + action + " " + user.userName);
                FollowUserModel nextUser = isDoUnfollow ? getNextUserUnToFollow() : getNextUserToFollow();
                followSingleUser(followerBot, isDoUnfollow, nextUser, webView, context, uiLogger);
            });
        });
    }

    public void getUsersToBeFollowed(GenricDataCallback uiLogger) {
        if (toBeFollowedQueue.size() > 10) {
            logger.onStart("Skip update queue request since queue is full " + toBeFollowedQueue.size());
            return;
        }
        GenericCompletableFuture<List<FollowUserModel>> userToBeFollowedFuture = getUserByFollowState(TableNames.FOLLOW_DATA, FollowUserState.TO_BE_FOLLOWED);
        userToBeFollowedFuture.thenAccept(newUsers -> {
            toBeFollowedQueue.addAll(newUsers);
            uiLogger.onStart("Added " + newUsers.size() + " to follow queue. Total = " + toBeFollowedQueue.size());
        });
    }

    //////////////// UNFOLLOW ///////////////////////
    public CompletableFuture<Void> setUserAsUnFollowed(FollowUserModel userModel) {
        userModel.followUserState = FollowUserState.UNFOLLOWED;
        userModel.unfollowDate = System.currentTimeMillis();
        serverDb.save((TableNames.MY_FOLLOWING_DATA), userModel);
        return serverDb.save((TableNames.FOLLOW_DATA), userModel);
    }

    public FollowUserModel getNextUserUnToFollow() {
        return toBeUnFollowedQueue.remove();
    }

    public void getUsersToBeUnFollowed(GenricDataCallback uiLogger, boolean fromFirebase) {
        if (toBeUnFollowedQueue.size() > 10) {
            logger.onStart("Skip update queue request since queue is full " + toBeFollowedQueue.size());
            return;
        }
        uiLogger.onStart("Please wait till follower data is loaded !!");

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


                if (fromFirebase) {
                    serverDb.query(TableNames.MY_FOLLOWING_DATA,
                            Arrays.asList(new WhereClause("tenant", GenericOperator.EQUAL, SemibitMediaApp.CURRENT_TENANT),
                                    new WhereClause("followUserState", GenericOperator.EQUAL, FollowUserState.FOLLOWED)),
                            FollowUserModel.class)
                            .thenAccept(toBeBanished -> {
                                toBeUnFollowedQueue.clear();
                                // filter to unfollow only automatically followed users
                                toBeUnFollowedQueue.addAll(toBeBanished.stream().filter(
                                        user -> followeIds.contains(String.valueOf(user.getId()))
                                ).collect(Collectors.toList()));
                                uiLogger.onStart("Added " + toBeUnFollowedQueue.size() + " from firebase to unfollow queue. Total = " + toBeUnFollowedQueue.size());
                            });
                    return;
                }
                GenericCompletableFuture<List<FollowUserModel>> usersFollowingMeFuture =
                        getConnectionsForUser(context.getString(R.string.username),
                                false, (onDone) -> {
                                }, logger);
                usersFollowingMeFuture.thenAccept(usersFollowingMe -> {
                    GenericCompletableFuture<List<FollowUserModel>> usersIAmFollowingFuture =
                            getConnectionsForUser(context.getString(R.string.username),
                                    true, (onDone) -> {
                                    }, logger);
                    usersIAmFollowingFuture.thenAccept(usersIAmFollowing -> {

                        List<FollowUserModel> toBeBanished = FollowerUtil.getUsersThatDontFollowMe(usersFollowingMe, usersIAmFollowing);
                        toBeUnFollowedQueue.clear();
                        // filter to unfollow only automatically followed users
                        toBeUnFollowedQueue.addAll(toBeBanished.stream().filter(
                                user -> followeIds.contains(String.valueOf(user.getId()))
                        ).collect(Collectors.toList()));
                        uiLogger.onStart("Added " + toBeUnFollowedQueue.size() + " to unfollow queue. Total = " + toBeUnFollowedQueue.size());

                    });

                });


            } catch (Exception e) {
                e.printStackTrace();
                uiLogger.onStart(e.getMessage());
            }
        });
    }

    public void startUnFollowingUsers(AdvancedWebView webView, TextView label, GenricDataCallback uiLogger) {
        AsyncTask.execute(() -> {
            final IGClient client = getIgClient();
            FollowerBot followerBot = new FollowerBot(client, s -> {
                Log.e("FollowerBot", "" + s);
                try {
                    label.setText(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            context.runOnUiThread(() -> {
                followSingleUser(followerBot, true, getNextUserUnToFollow(), webView, context, uiLogger);
            });
        });
    }


    //////////////// MARKING //////////////////////

    public void markUsersToFollowFromPost(String shortCode, GenricDataCallback cb, GenricDataCallback onUILog) {

        AsyncTask.execute(() -> {
            IGClient client = getIgClient();
            onUILog.onStart("Started marking users from post " + shortCode);
            CompletableFuture<LikeInfoResponse> completableFuture = new LikeInfoRequest(shortCode).execute(client);
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
                    }).filter(new OffensiveWordFilter(logger,context)).collect(Collectors.toList());

                    cb.onStart("done saved " + users.size());
                    saveUsersToBeFollowed(users, followersLists, onUILog);
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onStart("error " + e.getMessage());

                }
            });


        });
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
    public GenericCompletableFuture<List<FollowUserModel>> getConnectionsForUser(String userName, boolean isIncomingConnection, GenricDataCallback cb, GenricDataCallback onUILog) {

        GenericCompletableFuture<List<FollowUserModel>> usersResultFuture = new GenericCompletableFuture<>();
        AsyncTask.execute(() -> {
            try {
                IGClient client = getIgClient();
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
                    Log.d("FollowerBot", "Total " + connections + " Size = " + users.size());
                }

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

                GenericCompletableFuture<Void> onSave = serverDb.save(
                        isIncomingConnection ? TableNames.MY_FOLLOWING_DATA : TableNames.MY_FOLLOWERS_DATA
                        , new ArrayList<>(newUsers));
                onSave.thenAccept(v -> {
                    getUsersToBeUnFollowed(onUILog,true);
                    onUILog.onStart("Completed syncing IG " + connections + " " + newUsers.size());
                });

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

                usersResultFuture.complete(newUsers);
            } catch (Exception e) {
                e.printStackTrace();
                cb.onStart("error " + e.getMessage());
                onUILog.onStart(e.getMessage());
                usersResultFuture.completeExceptionally(e);
            }
        });
        return usersResultFuture;
    }

    public void markUsersToFollowFromFollowers(String userName, GenricDataCallback
            cb, GenricDataCallback onUILog) {

        int maxFollowersToLoad = 300;
        AsyncTask.execute(() -> {
            IGClient client = getIgClient();
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
                                new FollowerInfoRequest(String.valueOf(pk), 100, nextMaxId).execute(client);
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
                        }).filter(new OffensiveWordFilter(logger,context)).collect(Collectors.toList());

                        users.addAll(localUsers);
                        Log.d("FollowerBot", "Total Follower Size = " + users.size());

                    }
                    saveUsersToBeFollowed(users, followersLists, onUILog);

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

    public void saveUsersToBeFollowed(List<User> users, List<FollowersList>
            followeIdList, GenricDataCallback onUILog) {


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


    GenricDataCallback logger = s -> Log.e("FollowerBot", s);

}
