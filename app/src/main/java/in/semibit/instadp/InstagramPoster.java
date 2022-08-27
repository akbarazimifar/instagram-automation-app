package in.semibit.instadp;

import android.os.Environment;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.media.MediaAction;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.models.media.UploadParameters;
import com.github.instagram4j.instagram4j.requests.media.MediaConfigureTimelineRequest;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class InstagramPoster {

    IGClient client;
    GenricDataCallback callback;
    GenricDataCallback onCompleted;

    String last = "";
    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "instadp");

    public InstagramPoster(String username, String passwd, GenricDataCallback callback) {

        if (callback != null)
            this.callback = callback;
        else
            this.callback = System.out::println;
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            File file = new File(root.getAbsoluteFile(), "instagram.txt");
            if (file.exists()) {
                String split = Files.readAllBytes(Paths.get(file.getPath())).toString();
                username = split.split(",")[0].trim();
                passwd = split.split(",")[1].trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            File sessionFile = new File(root, "instagram_session.json");
            Path sessionFilePath = Paths.get(sessionFile.toURI());

            File fileClient = new File(root, "instagram_client.json");
            Path cientFile = Paths.get(fileClient.toURI());

            try {
                if (fileClient.exists() && sessionFile.exists()) {
                    client = IGClient.deserialize(fileClient, sessionFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (client == null) {
                client = IGClient.builder()
                        .username(username)
                        .password(passwd)
                        .login();

                client.serialize(fileClient, sessionFile);

            }
            this.callback.onStart("Logged In");
        } catch (Exception e) {
            e.printStackTrace();
            this.callback.onStart(e.getMessage());
        }
    }


    Gson gson = new Gson();

    public void post(File file, File cover, String caption, String mediaType, String bboy) {

        if (last.equals(file.getAbsolutePath())) {
            System.out.println("Skip reption !");
            this.callback.onStart("skipping repetition");
            return;
        }
        System.out.println("Posting started !");
        this.callback.onStart("Posting " + mediaType);

        last = file.getAbsolutePath();
        if (mediaType.equals("image")) {
            client.actions()
                    .timeline()
                    .uploadPhoto(file, caption)
                    .exceptionally(throwable -> {
                        this.callback.onStart("stop: error in upload " + throwable.getMessage());
                        throwable.printStackTrace();

                        return null;
                    })
                    .thenAccept(response -> {
                        if (response != null && response.getStatusCode() == 200) {
                            String code = response.getMedia().getCode();
                            //MainActivity.toast(null,"Successfully uploaded photo! " + code);
                            System.out.println("Successfully uploaded photo! " + code);
                            this.callback.onStart("stop: upload status code " + response.getStatus());
                            JSONObject bo = null;
                            try {
                                bo = new JSONObject(bboy);
                                bo.put("mediaId", response.getMedia().getPk());
                                bo.put("permalink", "https://www.instagram.com/p/" + code);
                                bo.put("short_code", code);
                                savePost(bo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        System.out.println("response of uploaded photo! " + response.getStatus());
                        this.callback.onStart("stop: upload status code " + response.getStatus());

                    })
                    .join();
        } else {
            try {
                client.actions()
                        .timeline()
                        .uploadVideoWithTimeout(Files.readAllBytes(Paths.get(file.toURI())), Files.readAllBytes(Paths.get(cover.toURI())),
                                new MediaConfigureTimelineRequest.MediaConfigurePayload().caption(caption), 20L)
                        .exceptionally(throwable -> {
                            this.callback.onStart("stop: error in upload " + throwable.getMessage());
                            throwable.printStackTrace();
                            return null;
                        })
                        .thenAccept(response -> {
                            if (response != null && response.getStatusCode() == 200) {
                                String code = response.getMedia().getCode();
                                //MainActivity.toast(null,"Successfully uploaded photo! " + code);
                                System.out.println("Successfully uploaded video! " + code);
                                this.callback.onStart("stop: upload status code " + response.getStatus());
                                JSONObject bo = null;
                                try {
                                    bo = new JSONObject(bboy);
                                    bo.put("mediaId", response.getMedia().getPk());
                                    bo.put("permalink", "https://www.instagram.com/p/" + code);
                                    bo.put("short_code", code);
                                    savePost(bo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            System.out.println("response of uploaded video! " + response.getStatus());
                            this.callback.onStart("stop: upload status code " + response.getStatus());

                        })
                        .join();
            } catch (Exception e) {
                e.printStackTrace();
                this.callback.onStart("stop: upload failed" + e.getMessage());

            }

        }

    }


    public void savePost(JSONObject bboy) {

        HashMap<String, String> ma = new HashMap<>();
        ma.put("body", bboy.toString());
        AndroidNetworking.upload("https://pasteitapp.herokuapp.com/semibitmedia/instagram/savePostInfo").addMultipartParameter(ma).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String body) {

                callback.onStart("Info Saved");
                //MainActivity.toast(null,"Info Saved");
            }

            @Override
            public void onError(ANError anError) {
                callback.onStart("Info Error while saving " + anError.getErrorBody());

                //MainActivity.toast(null,"Info Error while saving");

            }
        });

    }

}
