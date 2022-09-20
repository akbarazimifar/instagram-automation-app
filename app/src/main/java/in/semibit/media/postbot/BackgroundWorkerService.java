package in.semibit.media.postbot;


import static in.semibit.media.common.Insta4jClient.root;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.R;
import in.semibit.media.common.CommonAsyncExecutor;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.videoprocessor.VideoMerger;

public class BackgroundWorkerService extends Service {
    public InstagramPoster client;
    public Context context;
    public String ACTION_STOP_SERVICE = "199213";
    public static int NOTIF_ID = 1;

    VideoMerger videoMerger;

    private void copyAssets() {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            if (!(filename.contains("mp4") || filename.contains("mp3") || filename.contains("jpg") ||
                    filename.contains("png") || filename.contains("jpeg") || filename.contains("json") ||
                    filename.contains("txt"))) {
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(root, filename);
                if (outFile.exists()) {
                    continue;
                }
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private File getCover(File video) throws Exception {
        File cover = new File(root, "cover_" +video.getName() + ".jpg");
        ArrayList<Bitmap> frameList;
        int numeroFrameCaptured = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(video.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Exception= " + e);
        }
        // created an arraylist of bitmap that will store your frames
        frameList = new ArrayList<Bitmap>();
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int duration_millisec = Integer.parseInt(duration); //duration in millisec
        int duration_second = duration_millisec / 1000;  //millisec to sec.
        int frames_per_second = 2;  //no. of frames want to retrieve per second
        numeroFrameCaptured = frames_per_second * duration_second;
        Bitmap fram = retriever.getFrameAtTime(0);
        try (FileOutputStream fos = new FileOutputStream(cover)) {
            fram.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        }
        return cover;
    }

    public void work(Intent intent) {
        System.out.println("work started");

        client.callback = s -> {
            updateNotification(s);
            if (s.contains("stop")) {
                updateNotification(s);
                stopSelf();
            }
        };
        try {
            copyAssets();
        } catch (Exception e) {

        }
        File videoFile = new File(intent.getStringExtra("file"));
        File cover;
        try {
            cover = getCover(videoFile);

        } catch (Exception e) {
            e.printStackTrace();
            cover = new File(root, "cover_end.jpg");
            if (!cover.exists()) {
                updateNotification("Using stock cover file " + cover.getAbsolutePath());
            }
            System.out.println("assets copied");
        }
        videoMerger.setOnLog(client.callback);


        String endScreenFile= intent.getStringExtra("endscreen");
        if(endScreenFile == null || endScreenFile.isEmpty()){
            endScreenFile = "linkinbio.mp4";
        }
        File endScreen = new File(root,endScreenFile);

        CompletableFuture<File> onFileProcessed = videoMerger.merge("processed_"+videoFile.getName(), List.of(videoFile,endScreen));
        File finalCover = cover;
        onFileProcessed.exceptionally(e-> videoFile).thenAccept(outputFile->{
            client.post(outputFile, finalCover,
                    intent.getStringExtra("caption"),
                    intent.getStringExtra("mediaType"),
                    intent.getStringExtra("post"));
        });

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        videoMerger  = new VideoMerger(context);
//        CommonAsyncExecutor.execute(()->{
        if (client == null) {
            client = new InstagramPoster(Insta4jClient.getClient(context, null));
        }

        if (intent == null || ACTION_STOP_SERVICE.equals(intent.getAction())) {
            context.getSystemService(NotificationManager.class).cancel(NOTIF_ID);
            stopSelf();
        }
        this.startForeground(intent);
//        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startForeground(final Intent intent) {
        startForeground(NOTIF_ID, getMyActivityNotification(""));
        CommonAsyncExecutor.execute(() -> work(intent));
    }

    public Notification getMyActivityNotification(String text) {

        final String CHANNELID = "instagram-poster";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_HIGH
        );

        Intent stopSelf = new Intent(context, BackgroundWorkerService.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(context, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(context, CHANNELID)
                .setContentText(text.contains("stop") ? "Service Stopped" : "Service running")
                .setContentTitle(text)
                .setSmallIcon(R.drawable.icon);


        return notification.build();

    }

    /**
     * This is the method that can be called to update the Notification
     */
    public void updateNotification(String text) {

        Notification notification = getMyActivityNotification(text);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID++, notification);
    }

}
