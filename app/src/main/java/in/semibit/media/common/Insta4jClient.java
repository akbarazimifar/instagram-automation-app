package in.semibit.media.common;

import android.os.Environment;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.github.instagram4j.instagram4j.utils.SerializeUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import okhttp3.OkHttpClient;

public class Insta4jClient {

    private static IGClient client;
    public static File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "instadp");

    public static synchronized IGClient getClient(String username, String passwd, GenricDataCallback callback) {
        return getClient(username, passwd, false, callback);
    }

    public static synchronized IGClient getClient(String username, String passwd, boolean forceLogin, GenricDataCallback callback) {
        if (callback == null) {
            callback = (s) -> {
            };
        }
        if (client == null) {

            try {
                if (!root.exists()) {
                    root.mkdir();
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
                    if (fileClient.exists() && sessionFile.exists()) {
                        if (forceLogin) {
                            sessionFile.delete();
                            fileClient.delete();
                        }
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

                    client = IGClient.builder()
                            .client(okHttpClient)
                            .username(username)
                            .password(passwd)
                            .login();


                    client.serialize(fileClient, sessionFile);


                        SerializeUtil.serialize(client, fileClient);
                        SerializeUtil.serialize(client.getHttpClient().cookieJar(), sessionFile);
                }

                callback.onStart("Logged In");
            } catch (Exception e) {
                e.printStackTrace();
                callback.onStart(e.getMessage());
            }
        } else {
            callback.onStart("Logged In");
        }

        return client;
    }


}
