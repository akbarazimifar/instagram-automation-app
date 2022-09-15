package in.semibit.media.contentcreation;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.async.AsyncAction;
import com.github.instagram4j.instagram4j.actions.media.MediaAction;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.models.media.UploadParameters;
import com.github.instagram4j.instagram4j.requests.media.MediaConfigureTimelineRequest;
import com.github.instagram4j.instagram4j.requests.media.MediaConfigureToClipsRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.google.gson.Gson;
import com.semibit.ezandroidutils.EzUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import in.semibit.media.SemibitMediaApp;
import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.igclientext.post.MediaConfigureReelRemixRequest;
import in.semibit.media.common.igclientext.post.MediaConfigureToClipsRequestExt;
import in.semibit.media.followerbot.FollowBotService;

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
        if (last.equals(file.getAbsolutePath()) && !SemibitMediaApp.TEST_MODE) {
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
                                new MediaConfigureTimelineRequest.MediaConfigurePayload().caption(caption), 50L);

                if (true)
                    return;
                onMediaConfiguredResult.exceptionally(throwable -> {
                    this.callback.onStart("stop: error in upload " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                }).thenAccept(response -> {
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

        if (FollowBotService.TEST_MODE) {
            callback.onStart("Info Save Skipped since in Test Mode");
            return;
        }

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
                                                                                                  MediaConfigureTimelineRequest.MediaConfigurePayload mediPayload,
                                                                                                  long uploadFinishTimeoutSeconds) {
        String upload_id = String.valueOf(System.currentTimeMillis());
        MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload payload = new MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload().caption(mediPayload.caption());
        CompletableFuture<MediaResponse.MediaConfigureToClipsResponse> reelResponse = client.actions().upload()
                .videoWithCover(videoData, coverData, UploadParameters.forClip(upload_id))
                .thenCompose(response -> client.actions().upload().finish(upload_id))
                .thenCompose(response -> new MediaConfigureToClipsRequestExt(payload.upload_id(upload_id)).execute(client))
                .thenApply(CompletableFuture::completedFuture)
                .exceptionally(tr -> {
                    if (IGResponseException.IGFailedResponse.of(tr.getCause()).getStatusCode() != 202 &&
                            !(tr.getCause() instanceof SocketTimeoutException))
                        throw new CompletionException(tr.getCause());
                    return AsyncAction.retry(
                            () -> new MediaConfigureToClipsRequestExt(payload.upload_id(upload_id)).execute(client),
                            tr, 3, 10,
                            TimeUnit.SECONDS);
                })
                .thenCompose(Function.identity());

        try {
            MediaResponse.MediaConfigureToClipsResponse mediaResponse = reelResponse.join();
            EzUtils.log(mediaResponse.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

//        CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> postResponse =configureMediaToReelRemix(client, upload_id, mediPayload);
//        try {
//            CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> postResponseFuture =
//                    AsyncAction.retry(
//                            () -> MediaAction.configureMediaToTimeline(client, upload_id, mediPayload),
//                            null, 3, 10L,
//                            TimeUnit.SECONDS);
//
//            MediaResponse.MediaConfigureTimelineResponse postResponse = postResponseFuture.join();
//            return postResponseFuture;
//        } catch (Exception exception) {
//            exception.printStackTrace();
//            return null;
//        }
        return null;
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
