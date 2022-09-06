package in.semibit.instadp.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import in.semibit.instadp.R;

public abstract class BGService extends Service {

    Context context;
    int notifIdCounter;
    public BGService(){
        this.context = this;
    }

    public abstract void work(Intent intent);
    public abstract int getNotificationId();
    public abstract String getActionStopId();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getActionStopId().equals(intent.getAction())) {
            context.getSystemService(NotificationManager.class).cancel(getNotificationId());
            stopSelf();
        }
        this.startForeground(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startForeground(Intent intent) {
        notifIdCounter = getNotificationId();
        startForeground(notifIdCounter, getMyActivityNotification(""));
        new Thread(
                () -> {
                    Log.e("Service", getOverriddenClass().getName() + " is running...");
                    try {
                        work(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

    public Notification getMyActivityNotification(String text) {

        final String CHANNELID = "instagram-poster";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_HIGH
        );

        Intent stopSelf = new Intent(context, getOverriddenClass());
        stopSelf.setAction(this.getActionStopId());
        PendingIntent pStopSelf = PendingIntent.getService(context, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(context, CHANNELID)
                .setContentIntent(pStopSelf)
                .setContentText(text.contains("stop") ? "Service Stopped" : "Service running")
                .setContentTitle(text)
                .setSmallIcon(R.drawable.icon);


        return notification.build();

    }

    protected abstract Class<?> getOverriddenClass();

    /**
     * This is the method that can be called to update the Notification
     */
    public void updateNotification(String text,boolean isUpdateSameNotfi) {

        Notification notification = getMyActivityNotification(text);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(isUpdateSameNotfi ? getNotificationId() : ++notifIdCounter, notification);
    }

}
