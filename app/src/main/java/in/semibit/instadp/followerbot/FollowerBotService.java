package in.semibit.instadp.followerbot;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.github.instagram4j.instagram4j.IGClient;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import in.semibit.instadp.R;
import in.semibit.instadp.common.AdvancedWebView;
import in.semibit.instadp.common.Insta4jClient;
import io.objectbox.android.ObjectBoxDataSource;

public class FollowerBotService {


    Activity context;
    List<FollowUserModel> userToFollow;

    public FollowerBotService(Activity context) {
        this.context = context;

    }

    private IGClient getIgClient() {
        return Insta4jClient.getClient(context.getString(R.string.username), context.getString(R.string.password), null);
    }

    public AdvancedWebView generateAlert(final Activity context) {
        int layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,   // REMOVE FLAG_NOT_FOCUSABLE
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        View mFloatingWidget = LayoutInflater.from(context).inflate(R.layout.follower_bot, null);

        WindowManager mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
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

    public void markUsersToFollow(AdvancedWebView webView, Activity context) {
        IGClient client = getIgClient();
        Handler handler = new Handler();
        handler.post(() -> {
            client.actions().timeline().feed();
        });
    }

}
