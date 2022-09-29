package in.semibit.media.common;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.github.instagram4j.instagram4j.utils.StringCallback;
import com.semibit.ezandroidutils.EzUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import in.semibit.media.MainActivity;
import in.semibit.media.R;
import in.semibit.media.SemibitMediaApp;
import in.semibit.media.postbot.BackgroundWorkerService;
import kotlin.Pair;
import okhttp3.OkHttpClient;

public class Insta4jClient {

    private final static ConcurrentHashMap<String, IGClient> clients = new ConcurrentHashMap<String, IGClient>();
    public static File root = new File(Environment.getExternalStorageDirectory(), "instadp");
    public static Handler handler;

    public static synchronized IGClient getClient(Context context, String tenant, GenricDataCallback callback) {
        return getClient(context, tenant, false, callback);
    }

    public static List<Pair<String, String>> getAllTenants() {
        List<Pair<String, String>> tenants = new ArrayList<>();
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            File file = new File(root.getAbsoluteFile(), "instagram.txt");
            if (file.exists()) {
                String allLines = new String(Files.readAllBytes(Paths.get(file.getPath())));
                String[] lines = allLines.split("\n");
                for (String split : lines) {
                    String username = split.split(",")[0].trim();
                    String passwd = split.split(",")[1].trim();
                    if (!username.isEmpty() && !passwd.isEmpty()) {
                        tenants.add(new Pair<>(username, passwd));
                    }
                }
            } else {
                return List.of(new Pair<>(SemibitMediaApp.getAppContext().getString(R.string.username),
                        SemibitMediaApp.getAppContext().getString(R.string.password)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tenants;
    }

    public static synchronized IGClient getClient(Context context, String tenant, boolean forceLogin, GenricDataCallback callback) {
        if (callback == null) {
            callback = (s) -> {
            };
        }
        String username = context.getString(R.string.username);
        String passwd = context.getString(R.string.password);
        IGClient client = clients.get(tenant);
        String tenantPrefix = "instagram_" + tenant;
        if (client == null || forceLogin) {
            handler = new Handler(Looper.getMainLooper());
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
                    String allLines = new String(Files.readAllBytes(Paths.get(file.getPath())));
                    String[] lines = allLines.split("\n");
                    for (String split : lines) {
                        username = split.split(",")[0].trim();
                        passwd = split.split(",")[1].trim();
                        break;
                    }
                } else {
                    Files.write(Paths.get(file.getAbsolutePath()), (username + "," + passwd).getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Duration duration = Duration.of(60, ChronoUnit.SECONDS);

            try {
                File sessionFile = new File(root, tenantPrefix + "_session.json");
                File fileClient = new File(root, tenantPrefix + "_client.json");

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
                    } else {
                        LogsViewModel.addToLog("IG Client. Saved login missing");
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

                    LogsViewModel.addToLog("Logging in to " + username);


                    String usernameFinal = username;
                    IGClient.Builder.LoginHandler onTwoFactorHandler = new IGClient.Builder.LoginHandler() {
                        @Override
                        public LoginResponse accept(IGClient client, LoginResponse loginResponse) {
                            return IGChallengeUtils.resolveTwoFactor(client, loginResponse, askForInput(context, usernameFinal));
                        }
                    };
                    IGClient.Builder.LoginHandler onChallengeHandler = new IGClient.Builder.LoginHandler() {
                        @Override
                        public LoginResponse accept(IGClient client, LoginResponse loginResponse) {
                            return IGChallengeUtils.resolveChallenge(client, loginResponse, askForChallenge(context, usernameFinal), new StringCallback() {
                                @Override
                                public void onCb(String s) {
                                    EzUtils.toast(context, s);
                                }
                            });
                        }
                    };

                    client = IGClient.builder()
                            .onTwoFactor(onTwoFactorHandler)
                            .onChallenge(onChallengeHandler)
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
                LogsViewModel.addToLog("IGClient error logging in : " + e.getMessage());
                callback.onStart(e.getMessage());
            }
        } else {
            callback.onStart("Logged In");
        }

        if (client != null && client.selfProfile != null) {
            LogsViewModel.addToLog("Logged in success to " + client.selfProfile.getUsername());
            clients.put(tenant, client);
        }

        return client;
    }


    public static Callable<CompletableFuture<String>> askForChallenge(Context context, String username) {
        if (MainActivity.activity == null) {
            EzUtils.toast(context, "Activity is not running.");
            throw new RuntimeException("Unable to get code from dialog");
        }

        Callable<CompletableFuture<String>> onGetCode = () -> {

            CompletableFuture<String> onCode = new CompletableFuture<>();
            handler.post(() -> {
                LogsViewModel.addToLog("Trying to login using challenge");
                EzUtils.inputDialogBottom(MainActivity.activity, "Enter challenge code for " + username, EzUtils.TYPE_DEF, new EzUtils.InputDialogCallback() {
                    @Override
                    public void onDone(String text) {
                        onCode.complete(text.trim());
                    }
                });
            });
            return onCode;
        };

        return onGetCode;
    }


    public static Callable<CompletableFuture<String>> askForInput(Context context, String username) {
        if (MainActivity.activity == null) {
            EzUtils.toast(context, "Activity is not running.");
            throw new RuntimeException("Unable to get code from dialog");
        }

        Callable<CompletableFuture<String>> onGetCode = () -> {

            CompletableFuture<String> onCode = new CompletableFuture<>();
            handler.post(() -> {
                LogsViewModel.addToLog("Trying to login using 2FA");
                EzUtils.inputDialogBottom(MainActivity.activity, "Enter two factor code for " + username, EzUtils.TYPE_DEF, new EzUtils.InputDialogCallback() {
                    @Override
                    public void onDone(String text) {
                        onCode.complete(text.trim());
                    }
                });
            });
            return onCode;
        };

        return onGetCode;
    }

}
