package in.semibit.media.common;

import android.content.Context;
import android.os.Environment;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.utils.IGUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import in.semibit.media.R;
import in.semibit.media.postbot.BackgroundWorkerService;
import okhttp3.OkHttpClient;

public class Insta4jClient {

    private static IGClient client;
    public static File root = new File(Environment.getExternalStorageDirectory(), "instadp");

    public static synchronized IGClient getClient(Context context, GenricDataCallback callback) {
        return getClient(context, false, callback);
    }

    public static synchronized IGClient getClient(Context context, boolean forceLogin, GenricDataCallback callback) {
        if (callback == null) {
            callback = (s) -> {
            };
        }
        String username = context.getString(R.string.username);
        String passwd = context.getString(R.string.password);
        if (client == null || forceLogin) {

            try {
                if (!root.exists()) {
                    root.mkdir();

                    try {
                        BackgroundWorkerService.copyAssets(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                File file = new File(root.getAbsoluteFile(), "instagram.txt");
                if (file.exists()) {
                    String split = new String(Files.readAllBytes(Paths.get(file.getPath())));
                    username = split.split(",")[0].trim();
                    passwd = split.split(",")[1].trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Duration duration = Duration.of(60000, ChronoUnit.SECONDS);

            try {
                File sessionFile = new File(root, "instagram_session.json");
                File fileClient = new File(root, "instagram_client.json");


                try {
                    if (forceLogin) {
                        sessionFile.delete();
                        fileClient.delete();
                        client = null;
                    }
                    if (fileClient.exists() && sessionFile.exists()) {

                        LogsViewModel.addToLog("IG Client saved Login");
                        client = IGClient.deserialize(fileClient, sessionFile,
                                IGUtils.defaultHttpClientBuilder()
                                        .callTimeout(duration)
                                        .readTimeout(duration)
                                        .writeTimeout(duration)
                                        .connectTimeout(duration));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
               if (client == null) {

                    LogsViewModel.addToLog("IG Client WEB Login");
                    OkHttpClient okHttpClient = IGUtils.defaultHttpClientBuilder()
                            .callTimeout(duration)
                            .readTimeout(duration)
                            .writeTimeout(duration)
                            .connectTimeout(duration).build();

                    LogsViewModel.addToLog("Logging in to "+username);
                    client = IGClient.builder()
                            .client(okHttpClient)
                            .username(username)
                            .password(passwd)
                            .login();


                    client.serialize(fileClient, sessionFile);
//
//
//                        SerializeUtil.serialize(client, fileClient);
//                        SerializeUtil.serialize(client.getHttpClient().cookieJar(), sessionFile);
                }

                callback.onStart("Logged In");
            } catch (Exception e) {
                e.printStackTrace();
                LogsViewModel.addToLog("IGClient error logging in : "+e.getMessage());
                callback.onStart(e.getMessage());
            }
        } else {
            callback.onStart("Logged In");
        }

        return client;
    }


}
