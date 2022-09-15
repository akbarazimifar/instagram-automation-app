package in.semibit.media.contentcreation;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.media.UploadParameters;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.igclientext.post.MediaConfigureReelRemixRequest;

public class InstagramPoster {

    public IGClient client;
    public GenricDataCallback callback;

    String last = "";

    public InstagramPoster(IGClient client) {

        if (callback != null)
            this.callback = callback;
        else
            this.callback = System.out::println;

        this.client = client;

    }


    Gson gson = new Gson();

    public void post(File file, File cover, String caption, String mediaType, String bboy) {

        long startTime = System.currentTimeMillis();
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
                        long endTime = System.currentTimeMillis();
                        long totalTimeSecs = (endTime - startTime) / 1000;
                        if (response != null && response.getStatusCode() == 200) {
                            String code = response.getMedia().getCode();
                            //MainActivity.toast(null,"Successfully uploaded photo! " + code);
                            System.out.println("Successfully uploaded photo! " + code);
                            this.callback.onStart("stop: upload status code " + response.getStatus() + " (" + totalTimeSecs + " secs)");
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
                        try {

                            System.out.println("response of uploaded photo! " + response.getStatus());
                            this.callback.onStart("stop: upload status code " + response.getStatus() + " (" + totalTimeSecs + " secs)");
                        } catch (Exception e) {
                            e.printStackTrace();
                            this.callback.onStart("stop: error . Empty response");

                        }

                    })
                    .join();
        } else {
            try {
                CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> onMediaConfiguredResult =
                        uploadVideoWithTimeout(Files.readAllBytes(Paths.get(file.toURI())),
                                Files.readAllBytes(Paths.get(cover.toURI())),
                                new MediaConfigureReelRemixRequest.MediaConfigurePayload().caption(caption), 50L);

                onMediaConfiguredResult.exceptionally(throwable -> {
                    this.callback.onStart("stop: error in upload " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                })
                        .thenAccept(response -> {
                            long endTime = System.currentTimeMillis();
                            long totalTimeSecs = (endTime - startTime) / 1000;
                            if (response != null && response.getStatusCode() == 200) {
                                String code = response.getMedia().getCode();
                                //MainActivity.toast(null,"Successfully uploaded photo! " + code);
                                System.out.println("Successfully uploaded video! " + code);
                                this.callback.onStart("stop: upload status code " + response.getStatus() + " (" + totalTimeSecs + " secs)");
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
                            this.callback.onStart("stop: upload status code " + response.getStatus() + " (" + totalTimeSecs + " secs)");

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


    public CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> uploadVideoWithTimeout(byte[] videoData,
                                                                                                  byte[] coverData,
                                                                                                  MediaConfigureReelRemixRequest.MediaConfigurePayload payload,
                                                                                                  long uploadFinishTimeoutSeconds) {
        String upload_id = String.valueOf(System.currentTimeMillis());
        return client.actions().upload()
                .videoWithCover(videoData, coverData,
                        UploadParameters.forTimelineVideo(upload_id, false))
                .thenCompose(response -> {
                    IGUtils.sleepSeconds(uploadFinishTimeoutSeconds);

                    return client.actions().upload().finish(upload_id);
                })
                .thenCompose(response -> configureMediaToReelRemix(client, upload_id, payload));
    }


    public static CompletableFuture<MediaResponse.MediaConfigureTimelineResponse>
    configureMediaToReelRemix(IGClient client, String upload_id, MediaConfigureReelRemixRequest.MediaConfigurePayload payload) {
        CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> future = new CompletableFuture<>();

        ThreadGroup group = new ThreadGroup("threadGroup");
        new Thread(group, new Runnable() {
            @Override
            public void run() {
                CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> result =
                        new MediaConfigureReelRemixRequest(payload.upload_id(upload_id)).execute(client);
                MediaResponse.MediaConfigureTimelineResponse reponse = result.join();
                future.complete(reponse);
            }
        }, "YourThreadName", 48 * 1040 * 1024).start();
        return future;
    }
}
