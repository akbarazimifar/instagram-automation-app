package in.semibit.media.followerbot;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.instagram4j.instagram4j.IGClient;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.core.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import in.semibit.media.common.database.GenericCompletableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import in.semibit.media.R;
import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.common.database.DatabaseHelper;
import in.semibit.media.common.database.TableNames;
import in.semibit.media.common.database.WhereClause;
import in.semibit.media.common.igclientext.FollowersList;
import in.semibit.media.common.igclientext.followers.FollowerInfoRequest;
import in.semibit.media.common.igclientext.followers.FollowerInfoResponse;
import in.semibit.media.common.igclientext.followers.FollowingInfoRequest;
import in.semibit.media.common.igclientext.likes.LikeInfoRequest;
import in.semibit.media.common.igclientext.likes.LikeInfoResponse;
import in.semibit.media.common.igclientext.post.model.User;

public class FollowerBotService {


    Activity context;
    DatabaseHelper serverDb;
    DatabaseHelper localDb;
    List<FollowUserModel> userToFollow;

    public FollowerBotService(Activity context) {
        this.context = context;
        serverDb = new DatabaseHelper(Source.SERVER);
        localDb = new DatabaseHelper(Source.CACHE);
    }

    private IGClient getIgClient() {
        return Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), null);
    }

    View mFloatingWidget;

    public AdvancedWebView generateAlert(final Activity context) {

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
        if (mFloatingWidget != null) {
            try {
                mWindowManager.removeView(mFloatingWidget);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mFloatingWidget = LayoutInflater.from(context).inflate(R.layout.follower_bot, null);
        mWindowManager.addView(mFloatingWidget, params);


        final TextView label = mFloatingWidget.findViewById(R.id.label);
        final AdvancedWebView webView = mFloatingWidget.findViewById(R.id.webView);


        mFloatingWidget.setOnLongClickListener((v) -> {
            mWindowManager.removeView(mFloatingWidget);
            return true;
        });
        mFloatingWidget.setOnClickListener(c -> {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        int MAX_X_MOVE = 10;
        int MAX_Y_MOVE = 10;
        mFloatingWidget.setOnTouchListener(new View.OnTouchListener() {
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
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });

        return webView;

    }


    public boolean canIFollowNextUser() {
        return true;
    }

    public String getNextUserToFollow() {
        return "true";
    }

    public void startFollowingUsers(AdvancedWebView webView, TextView label) {
        new Handler(webView.getContext().getMainLooper()).post(() -> {

            final IGClient client = getIgClient();
            FollowerBot followerBot = new FollowerBot(client, s -> {
                Log.e("FollowerBot", "" + s);
                try {
                    label.setText(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            followSingleUser(followerBot, getNextUserToFollow(), webView, context);

        });
    }

    public void followSingleUser(FollowerBot followerBot, String user, AdvancedWebView webView, Activity context) {
        if (user != null || !canIFollowNextUser()) {
            return;
        }
        followerBot.followUnfollow(user, false, webView, context, s -> {
            Log.e("FollowerBot", "Follow Completed");
            String nextUser = getNextUserToFollow();
            followSingleUser(followerBot, nextUser, webView, context);
        });
    }

    public void markUsersToFollowFromPost(String shortCode, GenricDataCallback cb, GenricDataCallback onUILog) {

        AsyncTask.execute(() -> {
            IGClient client = getIgClient();
            CompletableFuture<LikeInfoResponse> completableFuture = new LikeInfoRequest(shortCode).execute(client);
            GenericCompletableFuture<List<FollowersList>> onLoadedFollowMeta = serverDb.query(TableNames.FOLLOW_META, Collections.singletonList(WhereClause.of("id", Filter.Operator.EQUAL, "to_be_follow")), FollowersList.class);
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
                FollowersList alreadyToBeFollow = followersLists.get(0);
                try {
                    String listCsvFollowers = String.join(",", alreadyToBeFollow.getFollowIds());

                    LikeInfoResponse postInfoResponse = completableFuture.get();
                    List<User> users = postInfoResponse.getLikers();
                    users = users.stream().filter(user -> !listCsvFollowers.contains(String.valueOf(user.getPk())))
                            .filter(new OffensiveWordFilter(logger)).collect(Collectors.toList());
                    cb.onStart("done saved " + users.size());
                    saveUsersToBeFollowed(users, alreadyToBeFollow, onUILog);
                } catch (Exception e) {
                    e.printStackTrace();
                    cb.onStart("error " + e.getMessage());

                }
            });


        });
    }

    public void getAllFollowersForUser(String userName, boolean isFollowingRequest, GenricDataCallback cb, GenricDataCallback onUILog) {

        AsyncTask.execute(() -> {
            try {
                IGClient client = getIgClient();
                String connections = isFollowingRequest ? "Followings" : "Followers";
                onUILog.onStart("Syncing " + connections + " from IG");

                com.github.instagram4j.instagram4j.models.user.User user = client.getActions().users().findByUsername(userName).get().getUser();
                Long pk = user.getPk();
                List<User> users = new ArrayList<>();
                String nextMaxId = "";
                boolean hasMoreFollowers = true;

                while (hasMoreFollowers && nextMaxId != null) {

                    CompletableFuture<FollowerInfoResponse> completableFuture =
                            (isFollowingRequest ? new FollowingInfoRequest(String.valueOf(pk), 100, nextMaxId)
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
                        .map(FollowUserModel::fromUserToBeFollowed).collect(Collectors.toList());

                GenericCompletableFuture<Void> onSave = serverDb.save(
                        isFollowingRequest ? TableNames.MY_FOLLOWING_DATA : TableNames.MY_FOLLOWERS_DATA
                        , new ArrayList<>(newUsers));
                onSave.thenAccept(v -> {
                    onUILog.onStart("Completed syncing IG " + connections + " " + newUsers.size());
                });

                if (isFollowingRequest) {
                    serverDb.save(TableNames.COUNTER, new FollowerCounter(DatabaseHelper.tablePrefix("following_count"), users.size()));
                } else {
                    serverDb.save(TableNames.COUNTER, new FollowerCounter(DatabaseHelper.tablePrefix("follower_count"), users.size()));
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
            }
        });
    }

    public void markUsersToFollowFromFollowers(String userName, GenricDataCallback
            cb, GenricDataCallback onUILog) {

        int maxFollowersToLoad = 300;
        AsyncTask.execute(() -> {
            IGClient client = getIgClient();

            GenericCompletableFuture<List<FollowersList>> onLoadedFollowMeta = serverDb.query(TableNames.FOLLOW_META, Collections.singletonList(WhereClause.of("id", Filter.Operator.EQUAL, "to_be_follow")), FollowersList.class);
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
                FollowersList alreadyToBeFollow = followersLists.get(0);
                try {

                    com.github.instagram4j.instagram4j.models.user.User user = client.getActions().users().findByUsername(userName).get().getUser();
                    Long pk = user.getPk();
                    List<User> users = new ArrayList<>();
                    String nextMaxId = "";
                    boolean hasMoreFollowers = true;
                    String listCsvFollowers = String.join(",", alreadyToBeFollow.getFollowIds());

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

                        List<User> localUsers = followerInfoResponse.getFollowers();
                        localUsers = localUsers.stream().filter(us -> !listCsvFollowers.contains(String.valueOf(us.getPk())))
                                .filter(new OffensiveWordFilter(logger)).collect(Collectors.toList());

                        users.addAll(localUsers);
                        Log.d("FollowerBot", "Total Follower Size = " + users.size());

                    }
                    saveUsersToBeFollowed(users, alreadyToBeFollow, onUILog);

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

    public void saveUsersToBeFollowed(List<User> users, FollowersList
            alreadyToBeFollow, GenricDataCallback onUILog) {


        List<FollowUserModel> newUsers = users.stream()
                .map(FollowUserModel::fromUserToBeFollowed).collect(Collectors.toList());

        try {
            alreadyToBeFollow.getFollowIds().addAll(newUsers.stream().map(u -> u.id).collect(Collectors.toList()));
            GenericCompletableFuture<Void> onSave = serverDb.save(TableNames.FOLLOW_DATA, new ArrayList<>(newUsers));
            onSave.thenAccept(v -> {
                onUILog.onStart("Completed marking users");
                String newUserNames = newUsers.stream().map(u -> u.userName).collect(Collectors.joining("\n"));
                onUILog.onStart(newUserNames);
                onUILog.onStart("Saved new followers to process " + newUsers.size());
            });
            serverDb.save(TableNames.FOLLOW_META, alreadyToBeFollow).thenAccept(v -> {
                onUILog.onStart("Saved meta of new followers to process " + newUsers.size());
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.onStart("Error Saving" + e.getMessage());
        }
    }


    GenricDataCallback logger = s -> Log.e("FollowerBot", s);
}
