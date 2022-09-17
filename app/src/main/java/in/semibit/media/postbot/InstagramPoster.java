package in.semibit.media.postbot;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.async.AsyncAction;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.models.media.UploadParameters;
import com.github.instagram4j.instagram4j.requests.media.MediaConfigureTimelineRequest;
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
import in.semibit.media.common.igclientext.post.MediaConfigureToClipsRequestExt;
import in.semibit.media.common.igclientext.post.PostInfoRequest;
import in.semibit.media.common.igclientext.post.PostInfoResponse;
import in.semibit.media.common.igclientext.post.model.PostItem;
import in.semibit.media.followerbot.FollowBotService;
import in.semibit.media.postbot.poc.ReelRequestHelper;

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

    public void post(File file, File cover, String caption, String mediaType, String postBodyProcessed) {

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
                                bo = new JSONObject(postBodyProcessed);
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

            processVideo(file, cover, caption, postBodyProcessed, startTime);
        }

    }

    private PostItem getAudioInfo(String shortCode) {

        try {
            PostInfoResponse postInfo =  new PostInfoRequest(shortCode).execute(client).join();
            return postInfo.getFirstPost();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }


    private void processVideo(File file, File cover, String caption, String postBodyProcessed,long startTime) {
        try {
            JSONObject postJsn = new JSONObject(postBodyProcessed);
            String short_code = postJsn.getString("source_short_code");
            //todo uncomment
            PostItem soundOriginalMedia =getAudioInfo(short_code);

            try {


                MediaResponse.MediaConfigureToClipsResponse onReels = uploadVideoToReels(
                        Files.readAllBytes(Paths.get(file.toURI())),
                        Files.readAllBytes(Paths.get(cover.toURI())),
                        new MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload()
                                .caption("Reel "+caption).originalMediaId(soundOriginalMedia),
                        soundOriginalMedia);
                if (onReels.getMedia() != null) {
                    this.callback.onStart("Uploaded to reels");
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.callback.onStart("Upload to reels failed");
            }


            //todo remove
            if(true)
                return;
            CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> onMediaConfiguredResult =
                    uploadVideoToTimeline(Files.readAllBytes(Paths.get(file.toURI())),
                            Files.readAllBytes(Paths.get(cover.toURI())),
                            new MediaConfigureTimelineRequest.MediaConfigurePayload().caption("Post "+caption));

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
                        bo = new JSONObject(postBodyProcessed);
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

            }).join();
        } catch (Exception e) {
            e.printStackTrace();
            this.callback.onStart("stop: upload failed" + e.getMessage());

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


    public MediaResponse.MediaConfigureToClipsResponse uploadVideoToReels(byte[] videoData,
                                                                          byte[] coverData,
                                                                          MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload mediPayload, PostItem soundOriginalMedia) {
        String upload_id = "1663361663077";//String.valueOf(System.currentTimeMillis());


        ReelRequestHelper reelRequestHelper = new ReelRequestHelper(client,upload_id);
        reelRequestHelper.clips_info_for_creation();
        reelRequestHelper.write_seen_state();
        reelRequestHelper.upload_settings();
        reelRequestHelper.configureToClip(soundOriginalMedia);

//
//        //todo remove
//        if(true)
//            return null;

//
//        CompletableFuture<MediaResponse.MediaConfigureToClipsResponse> reelResponse = client.actions().upload()
//                .videoWithCover(videoData, coverData, UploadParameters.forClip(upload_id))
//                .thenCompose(response -> client.actions().upload().finish(upload_id))
//
//
//                .thenCompose(reelRequestHelperResp->{
//                    reelRequestHelper.configureToClip(soundOriginalMedia);
//                    return CompletableFuture.completedFuture(null);
//                });


//                .thenCompose(response -> new MediaConfigureToClipsRequestExt(mediPayload.upload_id(upload_id)).execute(client))
//                .thenApply(CompletableFuture::completedFuture)
//                .exceptionally(tr -> {
//                    if (IGResponseException.IGFailedResponse.of(tr.getCause()).getStatusCode() != 202 &&
//                            !(tr.getCause() instanceof SocketTimeoutException))
//                        throw new CompletionException(tr.getCause());
//                    return AsyncAction.retry(
//                            () -> new MediaConfigureToClipsRequestExt(mediPayload.upload_id(upload_id)).execute(client),
//                            tr, 4, 10,
//                            TimeUnit.SECONDS);
//                })
//                .thenCompose(Function.identity());

//        try {
//            MediaResponse.MediaConfigureToClipsResponse mediaResponse = reelResponse.join();
//            EzUtils.log(mediaResponse.toString());
//            return mediaResponse;
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
        return null;
    }

    public CompletableFuture<MediaResponse.MediaConfigureTimelineResponse> uploadVideoToTimeline(byte[] videoData,
                                                                                                 byte[] coverData,
                                                                                                 MediaConfigureTimelineRequest.MediaConfigurePayload mediPayload) {
        try {
            return AsyncAction.retry(
                    () -> client.actions()
                            .timeline()
                            .uploadVideo(videoData, coverData, mediPayload),
                    null, 5, 10L,
                    TimeUnit.SECONDS);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
