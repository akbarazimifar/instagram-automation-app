package in.semibit.media.postbot.poc;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.gson.internal.LinkedTreeMap;
import com.semibit.ezandroidutils.EzUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.igclientext.post.MediaConfigureToClipsRequestExt;
import in.semibit.media.common.igclientext.post.model.PostItem;
import kotlin.Pair;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ReelRequestHelper {

    IGClient igClient;
    IGrequestHelper api;
    String upload_id;

    PostItem postItem;
    String audioAssetId = null;
    String remixOfPostMediaId = null;
    String audioArtistUserName = null;
    String audioTitle = null;

    boolean isDoRemix = true;

    String BASE_BODY_JSON_CONFIG = "";
    String audioAssetStartTimeMs = "0", audioClusterId = "";

    public ReelRequestHelper(IGClient client, String upload_id, PostItem sourcePost) {
        this.api = new IGrequestHelper(client);
        this.upload_id = upload_id;
        this.igClient = client;
        this.postItem = sourcePost;
        parsePost(postItem);
        EzUtils.e("Bot UPLOAD ID", upload_id);
    }


    public boolean isDoRemix() {
        return isDoRemix;
    }

    public void setDoRemix(boolean doRemix) {
        isDoRemix = doRemix;
    }

    public void parsePost(PostItem postItem) {

        // if you want to remix with the original sound artist
        //originalMediaId = "" + postItem.getClipsMetadata().getOriginalSoundInfo().getOriginalMediaId();
        try {
            remixOfPostMediaId = "" + postItem.getPk();
            if (postItem.getClipsMetadata() == null) {
                setDoRemix(false);
                return;
            }
            if (postItem.getClipsMetadata().getOriginalSoundInfo() != null) {
                audioAssetId = "" + postItem.getClipsMetadata().getOriginalSoundInfo().getAudioAssetId();
                audioArtistUserName = postItem.getClipsMetadata().getOriginalSoundInfo().getIgArtist().getUsername();
                audioTitle = postItem.getClipsMetadata().getOriginalSoundInfo().getOriginalAudioTitle();
                BASE_BODY_JSON_CONFIG = TEMPLATE_REMIXED_SONG;
            } else if (postItem.getClipsMetadata().getMusicInfo() != null) {
                LinkedTreeMap musicInfoMap = (LinkedTreeMap) postItem.getClipsMetadata().getMusicInfo();
                LinkedTreeMap musicAssetInfoMap = (LinkedTreeMap) musicInfoMap.get("music_asset_info");
                LinkedTreeMap musicConsumpInfoMap = (LinkedTreeMap) musicInfoMap.get("music_consumption_info");

                audioAssetId = musicAssetInfoMap.get("audio_asset_id").toString();
                audioArtistUserName = musicAssetInfoMap.get("ig_username").toString();
                audioTitle = musicAssetInfoMap.get("title").toString();
                audioClusterId = musicAssetInfoMap.get("audio_cluster_id").toString();

                try {
                    if (musicConsumpInfoMap != null)
                        audioAssetStartTimeMs = musicConsumpInfoMap.get("audio_asset_start_time_in_ms").toString();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                BASE_BODY_JSON_CONFIG = TEMPLATE_MUSIC_SOUND;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            setDoRemix(false);
        }
    }

    private HashMap<String, String> getFromSplitString(String split) {
        HashMap<String, String> map = new HashMap<>();
        try {
            String lines[] = split.split("\n");
            for (String line : lines) {
                String header[] = line.split(":");
                map.put(header[0].trim(), header[1].trim());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return map;
    }

    private String getUserId() {
        return "" + igClient.getSelfProfile().getPk();
    }

    private HashMap<String, String> getBasicHeaders() {
        HashMap<String, String> map = new HashMap<>(getFromSplitString(
                "Ig-U-Ds-User-Id: " + getUserId() + "\n" +
                        "Ig-Intended-User-Id: " + getUserId() + "\n" +
                        "Accept-Encoding: gzip, deflate\n" +
                        "X-Fb-Http-Engine: Liger\n" +
                        "X-Fb-Client-Ip: True"
        ));
        return map;
    }

    public String clips_info_for_creation() {
        String response = api.doIGGet("api/v1/clips/clips_info_for_creation/", getBasicHeaders());
        return response;
    }

    public String write_seen_state() {
        String mediaId = remixOfPostMediaId;
        String response = api.doIGPost("api/v1/clips/write_seen_state/",
                "{\"impressions\":\"[\\\"" + mediaId + "\\\"]\",\"_uid\":\"" + getUserId() + "\",\"_uuid\":\"" + UUID.randomUUID() + "\"}"
                , getBasicHeaders());
        return response;
    }

    public String upload_settings() {

        OkHttpClient okHttpClient = igClient.getHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"composer_session_id\":\"" + upload_id + "\",\"upload_setting_properties\":{\"upload_settings_version\":\"v0.1\",\"codec\":{\"video\\/avc\":[{\"encoder_name\":\"OMX.google.h264.encoder\",\"complexity\":[0,0],\"height_alignment\":2,\"width_alignment\":2,\"width\":[16,1920],\"max_instances\":32,\"bitrate\":[1,20000000],\"frame_rate\":[0,960],\"height\":[16,1088]}]},\"context\":{\"source_type\":\"clips\",\"target_id\":" + getUserId() + ",\"battery\":40,\"quality\":\"\"},\"video\":{\"video_height\":1920,\"video_gop_size_sec\":0,\"video_rotation_angle\":0,\"video_width\":1080,\"source_video_codec\":null,\"video_partial_frame_size_bytes\":16014,\"asset_id\":\"ED3DE9015026\",\"video_key_frame_size_bytes\":56509,\"target_duration\":29,\"video_original_file_size\":12755310,\"video_duration_milliseconds\":29,\"audio_bit_rate_bps\":-1,\"video_bit_rate_bps\":3625915,\"audio_codec_type\":null,\"video_fps\":30},\"creative_tools\":{\"transmuxing_eligible\":false,\"transcoding_required\":true,\"server_creative_tools_required\":false},\"network\":{\"download_latency_connection_quality\":\"ig_dummy\",\"network_connection_name\":\"ig_dummy\",\"download_bandwidth_connection_quality\":\"ig_dummy\",\"download_bandwidth\":7952}}}");

        try {
            Request.Builder okHttpReq = new Request.Builder()
                    .url("https://i.instagram.com/upload_settings/" + UUID.randomUUID())
                    .method("POST", body);
            okHttpReq.addHeader("Host", "i.instagram.com");
            okHttpReq.addHeader("X-Entity-Name", "upload_settings");
            okHttpReq.addHeader("X-Entity-Type", "application/json");
            okHttpReq.addHeader("X_fb_video_waterfall_id", upload_id + "_settings");
            okHttpReq.addHeader("Offset", "0");
            okHttpReq.addHeader("X-Entity-Length", "" + body.contentLength());
            okHttpReq.addHeader("X-Fb-Connection-Type", "WIFI");
            okHttpReq.addHeader("X-Ig-Connection-Type", "WIFI");
            okHttpReq.addHeader("X-Ig-Capabilities", "3brTv10=");
            okHttpReq.addHeader("X-Ig-App-Id", "567067343352427");
            okHttpReq.addHeader("Priority", "u=6, i");
            okHttpReq.addHeader("User-Agent", "Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)");
            okHttpReq.addHeader("Accept-Language", "en-US");
            okHttpReq.addHeader("Ig-U-Ds-User-Id", getUserId());
            okHttpReq.addHeader("Ig-Intended-User-Id", getUserId());
            okHttpReq.addHeader("Content-Type", "application/json");
            okHttpReq.addHeader("Content-Length", "" + body.contentLength());
            okHttpReq.addHeader("Accept-Encoding", "gzip, deflate");
            okHttpReq.addHeader("X-Fb-Http-Engine", "Liger");
            okHttpReq.addHeader("X-Fb-Client-Ip", "True");
            okHttpReq.addHeader("X-Fb-Server-Cluster", "True");
            okHttpReq.addHeader("Connection", "close");
            okHttpReq.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            okHttpReq.addHeader("Accept-Language", "en-US");
            okHttpReq.addHeader("User-Agent", igClient.getDevice().getUserAgent());
            okHttpReq.addHeader("X-IG-Connection-Type", "WIFI");
            okHttpReq.addHeader("X-Ads-Opt-Out", "0");
            okHttpReq.addHeader("X-CM-Bandwidth-KBPS", "-1.000");
            okHttpReq.addHeader("X-CM-Latency", "-1.000");
            okHttpReq.addHeader("X-IG-App-Locale", "en_US");
            okHttpReq.addHeader("X-IG-Device-Locale", "en_US");
            okHttpReq.addHeader("X-Pigeon-Session-Id", IGUtils.randomUuid());
            okHttpReq.addHeader("X-Pigeon-Rawclienttime", System.currentTimeMillis() + "");
            okHttpReq.addHeader("X-IG-Connection-Speed",
                    ThreadLocalRandom.current().nextInt(2000, 4000) + "kbps");
            okHttpReq.addHeader("X-IG-Bandwidth-Speed-KBPS", "-1.000");
            okHttpReq.addHeader("X-IG-Bandwidth-TotalBytes-B", "0");
            okHttpReq.addHeader("X-IG-Bandwidth-TotalTime-MS", "0");
            okHttpReq.addHeader("X-IG-Extended-CDN-Thumbnail-Cache-Busting-Value", "1000");
            okHttpReq.addHeader("X-IG-Device-ID", igClient.getGuid());
            okHttpReq.addHeader("X-IG-Android-ID", igClient.getDeviceId());
            okHttpReq.addHeader("X-FB-HTTP-engine", "Liger");
            Optional.ofNullable(igClient.getAuthorization())
                    .ifPresent(s -> okHttpReq.addHeader("Authorization", s));

            Request request = okHttpReq.build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                String res = response.body().string();
                res.length();
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String configureToClip(String caption) {
        try {
            LogsViewModel.addToLog("configureToClip executing");

            if (!isDoRemix()) {
                MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload medPayload = new MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload()
                        .caption(caption).originalMediaId(postItem);
                MediaResponse.MediaConfigureToClipsResponse res = new MediaConfigureToClipsRequestExt(medPayload.upload_id(upload_id)).execute(igClient).join();
                if (res.getMedia().getCaption() != null) {
                    return res.toString();
                } else {
                    return null;
                }
            }

            if (audioTitle == null) {
                audioTitle = "Original+audio";
            }
            if (remixOfPostMediaId == null || audioAssetId == null || audioArtistUserName == null)
                return "Insufficient info";
//            String s_AORIGPOSTMEDIAID = remixOfPostMediaId;//"" + postItem.getClipsMetadata().getOriginalSoundInfo().getOriginalMediaId();
//            String s_AAUDIOASSETID = audioAssetId;//"" + postItem.getClipsMetadata().getOriginalSoundInfo().getAudioAssetId();
//            String soundArtistUserName = audioArtistUserName;// postItem.getClipsMetadata().getOriginalSoundInfo().getIgArtist().getUsername();

            OkHttpClient okHttpClient = igClient.getHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
//            String bodyString = "signed_body=SIGNATURE."+BASE_BODY_JSON_CONFIG;
            String bodyString = BASE_BODY_JSON_CONFIG;

            bodyString = bodyString.replaceAll("AORIGPOSTMEDIAID", remixOfPostMediaId);
            bodyString = bodyString.replaceAll("AAUDIOASSETID", audioAssetId);
            bodyString = bodyString.replaceAll("AUPLOADID", upload_id);
            bodyString = bodyString.replaceAll("AUSERID", getUserId());
            bodyString = bodyString.replaceAll("ADEVICEID", igClient.getDeviceId());
            bodyString = bodyString.replaceAll("AUUID", UUID.randomUUID().toString());
            bodyString = bodyString.replaceAll("AAUDIOARTISTNAME", audioArtistUserName);

            bodyString = bodyString.replaceAll("AAUDIOCLUSTERID", audioClusterId);
            bodyString = bodyString.replaceAll("AUDIOASSETSTARTTIMEMS", audioAssetStartTimeMs);
            bodyString = bodyString.replaceAll("AAUDIOCLUSTERID", audioArtistUserName);
            bodyString = bodyString.replaceAll("AORIGINALAUDIOTITLE", audioTitle);

//            caption = "baldurs \ngate\n\n\n";
            JSONObject clean = new JSONObject(bodyString);
            clean.put("caption", caption);
            bodyString = clean.toString();//.replaceAll("ManualRemix",caption);


//            bodyString = bodyString.replaceAll("\n","");

            bodyString = IGUtils.generateSignature(bodyString);

//            bodyString = bodyString.replaceAll("EZIKIEL2517","%0A");

            RequestBody body = RequestBody.create(mediaType, bodyString);

            Request.Builder requestBuilder = new Request.Builder()
                    .url("https://i.instagram.com/api/v1/media/configure_to_clips/?video=1")
                    .method("POST", body)
                    .addHeader("Host", "i.instagram.com")
                    .addHeader("X-Ig-App-Locale", "en_US")
                    .addHeader("X-Ig-Device-Locale", "en_US")
                    .addHeader("X-Ig-Mapped-Locale", "en_US")
//                .addHeader("X-Pigeon-Session-Id", "UFS-f02b8f26-63cb-4b86-b51a-84b373dc86b4-0")
                    .addHeader("X-Pigeon-Rawclienttime", (System.currentTimeMillis() / 1000) + ".236")
                    .addHeader("X-Ig-Bandwidth-Speed-Kbps", ThreadLocalRandom.current().nextInt(2000, 4000) + ".000")
                    .addHeader("X-Ig-Bandwidth-Totalbytes-B", "" + ThreadLocalRandom.current().nextInt(2000, 4000) + "243")
                    .addHeader("X-Ig-Bandwidth-Totaltime-Ms", "" + ThreadLocalRandom.current().nextInt(2000, 4000))
                    .addHeader("X-Ig-App-Startup-Country", "IN")
                    .addHeader("X-Bloks-Version-Id", "ed06b936be88562bdc1a13aa16ef14521a460edaf0bd1c6d45748e2c542525a1")
//                .addHeader("X-Ig-Www-Claim", "hmac.AR24FJFvT95-zFkyiMmtSNfinGButbGI6Zyzo5TBsI_WfJ9t")
                    .addHeader("X-Bloks-Is-Layout-Rtl", "false")
//                .addHeader("X-Ig-Device-Id", "AUUID")
//                .addHeader("X-Ig-Family-Device-Id", "f4a03c5e-a471-4c32-b97c-9d6f2995fcf8")
//                .addHeader("X-Ig-Android-Id", "ADEVICEID")
                    .addHeader("X-Ig-Timezone-Offset", "19800")
                    .addHeader("X-Ig-Nav-Chain", "SelfFragment:self_profile:2:main_profile::,ContextualFeedFragment:feed_contextual_self_profile:5:button::,ClipsViewerFragment:clips_viewer_feed_contextual_self_profile:6:feed_contextual_self_profile::,TRUNCATEDx3,ClipsRemixOptionsFragment:clips_remix_options:10:button::,IgCameraViewController:reel_composer_camera:11:button::,ClipsCameraFragment:clips_precapture_camera:12:button::,VideoViewController:clips_postcapture_camera:13:button::,ClipsShareSheetFragment:clips_share_sheet:14:button::,IgCameraViewController:reel_composer_camera:15:button::,ClipsViewerFragment:clips_viewer_clips_tab:16:clips_tab::")
//                .addHeader("X-Ig-Salt-Ids", "51052545")
                    .addHeader("Is_clips_video", "1")
                    .addHeader("Retry_context", "{\"num_reupload\":0,\"num_step_auto_retry\":0,\"num_step_manual_retry\":0}")
                    .addHeader("X-Fb-Connection-Type", "WIFI")
                    .addHeader("X-Ig-Connection-Type", "WIFI")
                    .addHeader("X-Ig-Capabilities", "3brTv10=")
                    .addHeader("X-Ig-App-Id", "567067343352427")
                    .addHeader("Priority", "u=3")
//                .addHeader("User-Agent", "Instagram 252.0.0.17.111 Android (29/10; 400dpi; 1080x2040; Google/google; Android SDK built for x86; generic_x86; ranchu; en_US; 397702078)")
                    .addHeader("Accept-Language", "en-US")
//                    .addHeader("X-Mid", "YyImQwABAAHTYa3KnckXVbkVIu87")
                    .addHeader("Ig-U-Ig-Direct-Region-Hint", "ASH," + getUserId() + ",1694884196:01f76ac04d49354aade2c1ceca113231926e6917cb8322307c1424cedec080f5894dc0dc")
                    .addHeader("Ig-U-Shbid", "15628," + getUserId() + ",1694718419:01f7c629116c0471f0b16e94af3908231b4315e64199848a706e936feea04805efbe7b29")
                    .addHeader("Ig-U-Shbts", "1663182419," + getUserId() + ",1694718419:01f7391ecb88b7b7f31a5e39bf0f9d7d77835aaa77df3703e08d5f25af21a8721ca059da")
                    .addHeader("Ig-U-Ds-User-Id", getUserId())
                    .addHeader("Ig-U-Rur", "EAG," + getUserId() + ",1694885399:01f79f261246f155e3834c8515adb93ae55a03b1370c4933546f5625a588886f6c799b25")
                    .addHeader("Ig-Intended-User-Id", getUserId())
//                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .addHeader("X-Fb-Http-Engine", "Liger")
                    .addHeader("X-Fb-Client-Ip", "True")
                    .addHeader("X-Fb-Server-Cluster", "True")
//                .addHeader("Authorization", "Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTExMzI1NTQwNzIiLCJzZXNzaW9uaWQiOiI1MTEzMjU1NDA3MiUzQWxWMTlnSmlsS040S01nJTNBMTMlM0FBWWZhME9LRnVicjlSMUxnVlZ1SGF4aDBxWm5pUGtlT29QZy01UWNBSkEifQ==")
//                .addHeader("Cookie", "csrftoken=IZ4NEVRyURDHfXRMP5mppc1JwOen2jcz; ig_did=20177F9C-229D-414A-B3EB-FCD09087F5D2; ig_nrcb=1; mid=YwpBwgALAAFZj_GeFM3yHd5Q2oRR")


                    .addHeader("Ig-U-Ds-User-Id", getUserId())
                    .addHeader("Ig-Intended-User-Id", getUserId())
                    .addHeader("Content-Type", "application/json")
//                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Encoding", "utf-8")
                    .addHeader("X-Fb-Http-Engine", "Liger")
                    .addHeader("X-Fb-Client-Ip", "True")
                    .addHeader("X-Fb-Server-Cluster", "True")
                    .addHeader("Connection", "close")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("Accept-Language", "en-US")
                    .addHeader("User-Agent", igClient.getDevice().getUserAgent())
                    .addHeader("X-IG-Connection-Type", "WIFI")
                    .addHeader("X-Ads-Opt-Out", "0")
                    .addHeader("X-CM-Bandwidth-KBPS", "-1.000")
                    .addHeader("X-CM-Latency", "-1.000")
                    .addHeader("X-IG-App-Locale", "en_US")
                    .addHeader("X-IG-Device-Locale", "en_US")
                    .addHeader("X-Pigeon-Session-Id", IGUtils.randomUuid())
                    .addHeader("X-Pigeon-Rawclienttime", System.currentTimeMillis() + "")
                    .addHeader("X-IG-Connection-Speed",
                            ThreadLocalRandom.current().nextInt(2000, 4000) + "kbps")
                    .addHeader("X-IG-Bandwidth-Speed-KBPS", "-1.000")
                    .addHeader("X-IG-Bandwidth-TotalBytes-B", "0")
                    .addHeader("X-IG-Bandwidth-TotalTime-MS", "0")
                    .addHeader("X-IG-Extended-CDN-Thumbnail-Cache-Busting-Value", "1000")
                    .addHeader("X-IG-Device-ID", igClient.getGuid())
                    .addHeader("X-IG-Android-ID", igClient.getDeviceId())
                    .addHeader("X-FB-HTTP-engine", "Liger")
//                .addHeader("X-Ig-Www-Claim","hmac.AR2_73UwszrOua4bBYwNRU0cPHGM0aA2Qd--mtVft0mnoEcD")
                    .addHeader("Authorization", igClient.getAuthorization())

                   ;

            for(Map.Entry<String, String> header:igClient.getDynamicHeaders().entrySet()){
                requestBuilder.addHeader(header.getKey(),header.getValue());
            }

            Request request = requestBuilder.build();
//
//        try {
//            String response = api.doIGPost("api/v1/media/configure_to_clips/?video=1",
//                    bodyString,
//                    false
//                    , getBasicHeaders());
//            return response;
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//

            Response response = okHttpClient.newCall(request).execute();
            String res = response.body().string();
            LogsViewModel.addToLog("REEL CONFIGURE RESPONSE " + res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            LogsViewModel.addToLog("REEL CONFIGURE ERROR " + e.getMessage());
        }
        return null;
    }

    public String TEMPLATE_REMIXED_SONG = "{\n" +
            "    \"clips_share_preview_to_feed\": \"1\",\n" +
            "    \"is_shared_to_fb\": \"0\",\n" +
            "    \"is_clips_edited\": \"0\",\n" +
            "    \"like_and_view_counts_disabled\": \"0\",\n" +
            "    \"camera_entry_point\": \"299\",\n" +
            "    \"tap_models\": \"[{\\\"x\\\":0.0,\\\"y\\\":0.0,\\\"z\\\":0,\\\"width\\\":0.0,\\\"height\\\":0.0,\\\"rotation\\\":0.0,\\\"type\\\":\\\"music\\\",\\\"tag\\\":\\\"386fc201-6d73-43ad-83a4-a3291ccf5488\\\",\\\"audio_asset_start_time_in_ms\\\":0,\\\"audio_asset_suggested_start_time_in_ms\\\":0,\\\"derived_content_start_time_in_ms\\\":0,\\\"overlap_duration_in_ms\\\":15000,\\\"browse_session_id\\\":\\\"d46be47e-3b30-479d-9403-166e1af80caf\\\",\\\"music_product\\\":\\\"clips_camera_format_v2\\\",\\\"audio_asset_id\\\":\\\"AAUDIOASSETID\\\",\\\"progressive_download_url\\\":\\\"https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/299997302_1479800132448155_5706075520205073543_n.m4a?_nc_cat=106&ccb=1-7&_nc_sid=02c1ff&_nc_ohc=zHeGZvfYiKwAX8nMjJK&_nc_ad=z-m&_nc_cid=0&_nc_ht=scontent-maa2-1.xx&oh=00_AT-XviAjHpxHaf_VMd5VImEFTkxDvEXE3sB6sWrSg5nUsg&oe=6329E320\\\",\\\"duration_in_ms\\\":28026,\\\"dash_manifest\\\":\\\"<?xml version=\\\\\\\"1.0\\\\\\\" encoding=\\\\\\\"UTF-8\\\\\\\"?>\\\\n<!--Generated with https://github.com/google/shaka-packager version v1.6.0-release-->\\\\n<MPD xmlns=\\\\\\\"urn:mpeg:dash:schema:mpd:2011\\\\\\\" xmlns:xsi=\\\\\\\"http://www.w3.org/2001/XMLSchema-instance\\\\\\\" xmlns:xlink=\\\\\\\"http://www.w3.org/1999/xlink\\\\\\\" xmlns:cenc=\\\\\\\"urn:mpeg:cenc:2013\\\\\\\" xsi:schemaLocation=\\\\\\\"urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd\\\\\\\" profiles=\\\\\\\"urn:mpeg:dash:profile:isoff-on-demand:2011\\\\\\\" minBufferTime=\\\\\\\"PT2S\\\\\\\" type=\\\\\\\"static\\\\\\\" mediaPresentationDuration=\\\\\\\"PT28.025S\\\\\\\">\\\\n  <Period id=\\\\\\\"0\\\\\\\">\\\\n    <AdaptationSet id=\\\\\\\"0\\\\\\\" contentType=\\\\\\\"audio\\\\\\\" subsegmentAlignment=\\\\\\\"true\\\\\\\">\\\\n      <Representation id=\\\\\\\"0\\\\\\\" bandwidth=\\\\\\\"70239\\\\\\\" codecs=\\\\\\\"mp4a.40.2\\\\\\\" mimeType=\\\\\\\"audio/mp4\\\\\\\" audioSamplingRate=\\\\\\\"22050\\\\\\\">\\\\n        <AudioChannelConfiguration schemeIdUri=\\\\\\\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\\\\\\\" value=\\\\\\\"2\\\\\\\"/>\\\\n        <BaseURL>https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/300112693_1369990176858319_7851170966804425913_n.m4a?_nc_cat=100&amp;ccb=1-7&amp;_nc_sid=02c1ff&amp;_nc_ohc=ztGF8NvwPM0AX8Qecel&amp;_nc_ad=z-m&amp;_nc_cid=0&amp;_nc_ht=scontent-maa2-1.xx&amp;oh=00_AT-KYeTPtrqlaTT72PM4s9GPmTsSIYXNLzNRx9bz1u5SzA&amp;oe=6329985D</BaseURL>\\\\n        <SegmentBase indexRange=\\\\\\\"743-942\\\\\\\" timescale=\\\\\\\"44100\\\\\\\">\\\\n          <Initialization range=\\\\\\\"0-742\\\\\\\"/>\\\\n        </SegmentBase>\\\\n      </Representation>\\\\n    </AdaptationSet>\\\\n  </Period>\\\\n</MPD>\\\\n\\\",\\\"title\\\":\\\"AORIGINALAUDIOTITLE\\\",\\\"display_artist\\\":\\\"AAUDIOARTISTNAME\\\",\\\"cover_artwork_uri\\\":\\\"https://scontent-maa2-2.cdninstagram.com/v/t51.2885-19/272657676_2469488359849327_6306960314298840502_n.jpg?stp=dst-jpg_s150x150&efg=eyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ&_nc_ht=scontent-maa2-2.cdninstagram.com&_nc_cat=107&_nc_ohc=pxhM5guzEowAX-Vrx6j&edm=AOM4IUYBAAAA&ccb=7-5&oh=00_AT_t30Y8FMomzq6DrVqIj428BWXreiqt0sR0MyH-FycpjQ&oe=632AFEBF&_nc_sid=734323\\\",\\\"cover_artwork_thumbnail_uri\\\":\\\"https://scontent-maa2-2.cdninstagram.com/v/t51.2885-19/272657676_2469488359849327_6306960314298840502_n.jpg?stp=dst-jpg_s150x150&efg=eyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ&_nc_ht=scontent-maa2-2.cdninstagram.com&_nc_cat=107&_nc_ohc=pxhM5guzEowAX-Vrx6j&edm=AOM4IUYBAAAA&ccb=7-5&oh=00_AT_t30Y8FMomzq6DrVqIj428BWXreiqt0sR0MyH-FycpjQ&oe=632AFEBF&_nc_sid=734323\\\",\\\"is_explicit\\\":false,\\\"has_lyrics\\\":false,\\\"is_original_sound\\\":true,\\\"is_local_audio\\\":false,\\\"allows_saving\\\":false,\\\"original_media_id\\\":\\\"AORIGPOSTMEDIAID\\\",\\\"hide_remixing\\\":false,\\\"picked_in_post_capture\\\":false,\\\"is_bookmarked\\\":false,\\\"should_mute_audio\\\":false,\\\"product\\\":\\\"story_camera_clips_v2\\\",\\\"is_sticker\\\":false,\\\"display_type\\\":\\\"HIDDEN\\\",\\\"tap_state\\\":0,\\\"tap_state_str_id\\\":\\\"\\\"}]\",\n" +
            "    \"is_created_with_sound_sync\": \"0\",\n" +
            "    \"filter_type\": \"0\",\n" +
            "    \"camera_session_id\": \"0ddef447-b2de-426b-9b41-28a9c943a74e\",\n" +
            "    \"disable_comments\": \"0\",\n" +
            "    \"clips_creation_entry_point\": \"clips\",\n" +
            "    \"timezone_offset\": \"19800\",\n" +
            "    \"source_type\": \"3\",\n" +
            "    \"camera_position\": \"unknown\",\n" +
            "    \"video_result\": \"\",\n" +
            "    \"is_created_with_contextual_music_recs\": \"0\",\n" +
            "    \"_uid\": \"AUSERID\",\n" +
            "    \"device_id\": \"ADEVICEID\",\n" +
            "    \"_uuid\": \"AUUID\",\n" +
            "    \"nav_chain\": \"SelfFragment:self_profile:2:main_profile::,ContextualFeedFragment:feed_contextual_self_profile:5:button::,ClipsViewerFragment:clips_viewer_feed_contextual_self_profile:6:feed_contextual_self_profile::,TRUNCATEDx1,ClipsRemixOptionsFragment:clips_remix_options:8:button::,ClipsViewerFragment:clips_viewer_original_creator_video:9:button::,ClipsRemixOptionsFragment:clips_remix_options:10:button::,IgCameraViewController:reel_composer_camera:11:button::,ClipsCameraFragment:clips_precapture_camera:12:button::,VideoViewController:clips_postcapture_camera:13:button::,ClipsShareSheetFragment:clips_share_sheet:14:button::\",\n" +
            "    \"caption\": \"ManualRemix\",\n" +
            "    \"video_subtitles_enabled\": \"1\",\n" +
            "    \"capture_type\": \"clips_v2\",\n" +
            "    \"audience\": \"default\",\n" +
            "    \"upload_id\": \"AUPLOADID\",\n" +
            "    \"template_clips_media_id\": \"null\",\n" +
            "    \"is_creator_requesting_mashup\": \"0\",\n" +
            "    \"additional_audio_info\": {\n" +
            "        \"has_voiceover_attribution\": \"0\"\n" +
            "    },\n" +
            "    \"device\": {\n" +
            "        \"manufacturer\": \"Samsung\",\n" +
            "        \"model\": \"Samsung Galaxy\",\n" +
            "        \"android_version\": 29,\n" +
            "        \"android_release\": \"10\"\n" +
            "    },\n" +
            "    \"mashup_info\": {\n" +
            "        \"original_media_id\": \"AORIGPOSTMEDIAID\",\n" +
            "        \"original_media_duration\": 27957,\n" +
            "        \"original_media_is_shared_to_facebook\": false,\n" +
            "        \"are_remixes_crosspostable\": false,\n" +
            "        \"source_media_creation_state\": \"SEQUENTIAL_REMIX\",\n" +
            "        \"original_media_is_photo\": false,\n" +
            "        \"mashup_type\": \"sequential\"\n" +
            "    },\n" +
            "    \"length\": 28.118,\n" +
            "    \"clips\": [\n" +
            "        {\n" +
            "            \"length\": 28.118,\n" +
            "            \"source_type\": \"3\",\n" +
            "            \"camera_position\": \"back\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"extra\": {\n" +
            "        \"source_width\": 1080,\n" +
            "        \"source_height\": 1920\n" +
            "    },\n" +
            "    \"remixed_original_sound_params\": {\n" +
            "        \"original_media_id\": \"AORIGPOSTMEDIAID\"\n" +
            "    },\n" +
            "    \"audio_muted\": false,\n" +
            "    \"poster_frame_index\": 0,\n" +
            "    \"clips_segments_metadata\": {\n" +
            "        \"num_segments\": 2,\n" +
            "        \"clips_segments\": [\n" +
            "            {\n" +
            "                \"index\": 0,\n" +
            "                \"face_effect_id\": null,\n" +
            "                \"speed\": 100,\n" +
            "                \"source_type\": \"0\",\n" +
            "                \"duration_ms\": 28025,\n" +
            "                \"audio_type\": \"original_remix\",\n" +
            "                \"from_draft\": \"0\",\n" +
            "                \"camera_position\": -1,\n" +
            "                \"media_folder\": null,\n" +
            "                \"media_type\": \"video\",\n" +
            "                \"original_media_type\": 2\n" +
            "            },\n" +
            "            {\n" +
            "                \"index\": 1,\n" +
            "                \"face_effect_id\": null,\n" +
            "                \"speed\": 100,\n" +
            "                \"source_type\": \"1\",\n" +
            "                \"duration_ms\": 93,\n" +
            "                \"audio_type\": \"original_remix\",\n" +
            "                \"from_draft\": \"0\",\n" +
            "                \"camera_position\": 1,\n" +
            "                \"media_folder\": null,\n" +
            "                \"media_type\": \"video\",\n" +
            "                \"original_media_type\": 2\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"clips_audio_metadata\": {\n" +
            "        \"original\": {\n" +
            "            \"volume_level\": 1.0\n" +
            "        },\n" +
            "        \"remix\": {\n" +
            "            \"volume_level\": 1.0,\n" +
            "            \"is_saved\": \"0\",\n" +
            "            \"artist_name\": \"AAUDIOARTISTNAME\",\n" +
            "            \"audio_asset_id\": \"AAUDIOASSETID\",\n" +
            "            \"audio_cluster_id\": null,\n" +
            "            \"track_name\": \"Original audio\",\n" +
            "            \"is_picked_precapture\": \"1\",\n" +
            "            \"original_media_id\": \"AORIGPOSTMEDIAID\"\n" +
            "        }\n" +
            "    }\n" +
            "}";


    String TEMPLATE_MUSIC_SOUND = "{\n" +
            "    \"clips_share_preview_to_feed\": \"1\",\n" +
            "    \"is_shared_to_fb\": \"0\",\n" +
            "    \"is_clips_edited\": \"0\",\n" +
            "    \"like_and_view_counts_disabled\": \"0\",\n" +
            "    \"camera_entry_point\": \"299\",\n" +
            "    \"tap_models\": \"[{\\\"x\\\":0.0,\\\"y\\\":0.0,\\\"z\\\":0,\\\"width\\\":0.0,\\\"height\\\":0.0,\\\"rotation\\\":0.0,\\\"type\\\":\\\"music\\\",\\\"tag\\\":\\\"93eb7713-58fa-4621-9fa9-9663283d09e7\\\",\\\"audio_asset_start_time_in_ms\\\":AUDIOASSETSTARTTIMEMS,\\\"audio_asset_suggested_start_time_in_ms\\\":32000,\\\"derived_content_start_time_in_ms\\\":0,\\\"overlap_duration_in_ms\\\":15000,\\\"browse_session_id\\\":\\\"a9304722-4734-4675-b1d3-90296712371c\\\",\\\"music_product\\\":\\\"clips_camera_format_v2\\\",\\\"audio_asset_id\\\":\\\"AAUDIOASSETID\\\",\\\"audio_cluster_id\\\":\\\"AAUDIOCLUSTERID\\\",\\\"progressive_download_url\\\":\\\"https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/241326306_547905612985617_1799686547546246321_n.m4a?_nc_cat=1&ccb=1-7&_nc_sid=02c1ff&_nc_ohc=pss4xvRe0TkAX_WRaYJ&_nc_ad=z-m&_nc_cid=0&_nc_ht=scontent-maa2-1.xx&oh=00_AT-Vn4MKHSMmqiO_QuBv3dCQEFKwbNxAAxhWgIZiS_c9VQ&oe=632ADD5F\\\",\\\"duration_in_ms\\\":188368,\\\"dash_manifest\\\":\\\"<?xml version=\\\\\\\"1.0\\\\\\\" encoding=\\\\\\\"UTF-8\\\\\\\"?>\\\\n<!--Generated with https://github.com/google/shaka-packager version v1.6.0-release-->\\\\n<MPD xmlns=\\\\\\\"urn:mpeg:dash:schema:mpd:2011\\\\\\\" xmlns:xsi=\\\\\\\"http://www.w3.org/2001/XMLSchema-instance\\\\\\\" xmlns:xlink=\\\\\\\"http://www.w3.org/1999/xlink\\\\\\\" xmlns:cenc=\\\\\\\"urn:mpeg:cenc:2013\\\\\\\" xsi:schemaLocation=\\\\\\\"urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd\\\\\\\" profiles=\\\\\\\"urn:mpeg:dash:profile:isoff-on-demand:2011\\\\\\\" minBufferTime=\\\\\\\"PT2S\\\\\\\" type=\\\\\\\"static\\\\\\\" mediaPresentationDuration=\\\\\\\"PT188.412S\\\\\\\">\\\\n  <Period id=\\\\\\\"0\\\\\\\">\\\\n    <AdaptationSet id=\\\\\\\"0\\\\\\\" contentType=\\\\\\\"audio\\\\\\\" subsegmentAlignment=\\\\\\\"true\\\\\\\">\\\\n      <Representation id=\\\\\\\"0\\\\\\\" bandwidth=\\\\\\\"130014\\\\\\\" codecs=\\\\\\\"mp4a.40.2\\\\\\\" mimeType=\\\\\\\"audio/mp4\\\\\\\" audioSamplingRate=\\\\\\\"48000\\\\\\\">\\\\n        <AudioChannelConfiguration schemeIdUri=\\\\\\\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\\\\\\\" value=\\\\\\\"2\\\\\\\"/>\\\\n        <BaseURL>https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/241264674_544578393299033_1055633617535453105_n.m4a?_nc_cat=1&amp;ccb=1-7&amp;_nc_sid=02c1ff&amp;_nc_ohc=lONrSb7QROYAX9zjNOF&amp;_nc_ad=z-m&amp;_nc_cid=0&amp;_nc_ht=scontent-maa2-1.xx&amp;oh=00_AT-M3rpxPa256CB4hO2P_7QQ31QK22RioG_hM8cABajmFQ&amp;oe=6329EE3B</BaseURL>\\\\n        <SegmentBase indexRange=\\\\\\\"741-1900\\\\\\\" timescale=\\\\\\\"48000\\\\\\\">\\\\n          <Initialization range=\\\\\\\"0-740\\\\\\\"/>\\\\n        </SegmentBase>\\\\n      </Representation>\\\\n    </AdaptationSet>\\\\n  </Period>\\\\n</MPD>\\\\n\\\",\\\"highlight_start_times_in_ms\\\":[32000,47000,11000],\\\"title\\\":\\\"AORIGINALAUDIOTITLE\\\",\\\"display_artist\\\":\\\"CKay\\\",\\\"cover_artwork_uri\\\":\\\"https://cdn.fbsbx.com/v/t65.14500-21/241201178_1189544901537377_7266854393852552594_n.jpg?stp=cp0_dst-jpg_e15_p526x296_q65&_nc_cat=1&ccb=1-7&_nc_sid=cbead8&_nc_ohc=lVNL6EfrlKIAX-vt8Ig&_nc_ht=cdn.fbsbx.com&oh=00_AT_TQfsoeWiPnxjGV04PeCKJnk9hvO7ow8TgE0tfiuezrw&oe=6327A3F2\\\",\\\"cover_artwork_thumbnail_uri\\\":\\\"https://cdn.fbsbx.com/v/t65.14500-21/241201178_1189544901537377_7266854393852552594_n.jpg?stp=cp0_dst-jpg_e15_q65_s168x128&_nc_cat=1&ccb=1-7&_nc_sid=cbead8&_nc_ohc=lVNL6EfrlKIAX-vt8Ig&_nc_ht=cdn.fbsbx.com&oh=00_AT9ChbPJ49c-p_FTSAbWmhW4x1CByLFT8E1y6GUXdxdbtQ&oe=6327A3F2\\\",\\\"is_explicit\\\":false,\\\"has_lyrics\\\":false,\\\"is_original_sound\\\":false,\\\"is_local_audio\\\":false,\\\"allows_saving\\\":true,\\\"hide_remixing\\\":false,\\\"picked_in_post_capture\\\":false,\\\"is_bookmarked\\\":false,\\\"should_mute_audio\\\":false,\\\"product\\\":\\\"story_camera_clips_v2\\\",\\\"is_sticker\\\":false,\\\"display_type\\\":\\\"HIDDEN\\\",\\\"tap_state\\\":0,\\\"tap_state_str_id\\\":\\\"\\\"}]\",\n" +
            "    \"is_created_with_sound_sync\": \"0\",\n" +
            "    \"filter_type\": \"0\",\n" +
            "    \"camera_session_id\": \"6988583e-246e-471d-b624-e876c66d7a6f\",\n" +
            "    \"disable_comments\": \"0\",\n" +
            "    \"clips_creation_entry_point\": \"clips\",\n" +
            "    \"timezone_offset\": \"19800\",\n" +
            "    \"source_type\": \"3\",\n" +
            "    \"camera_position\": \"unknown\",\n" +
            "    \"video_result\": \"\",\n" +
            "    \"is_created_with_contextual_music_recs\": \"0\",\n" +
            "    \"_uid\": \"AUSERID\",\n" +
            "    \"device_id\": \"ADEVICEID\",\n" +
            "    \"_uuid\": \"AUUID\",\n" +
            "    \"nav_chain\": \"MainFeedFragment:feed_timeline:1:cold_start::,ShortUrlFeedFragment:feed_short_url:5:warm_start::,MainFeedFragment:feed_timeline:6:button::,TRUNCATEDx4,IgCameraViewController:reel_composer_camera:11:button::,ClipsCameraFragment:clips_precapture_camera:12:button::,VideoViewController:clips_postcapture_camera:13:button::,ClipsShareSheetFragment:clips_share_sheet:14:button::,ClipsCoverPhotoPickerFragment:clips_cover_photo_picker_fragment:15:button::,ClipsShareSheetFragment:clips_share_sheet:16:button::,ClipsShareSheetFragment:clips_share_sheet:17:button::\",\n" +
            "    \"caption\": \"GREENMANGOESYUM\",\n" +
            "    \"video_subtitles_enabled\": \"1\",\n" +
            "    \"capture_type\": \"clips_v2\",\n" +
            "    \"audience\": \"default\",\n" +
            "    \"upload_id\": \"AUPLOADID\",\n" +
            "    \"template_clips_media_id\": \"null\",\n" +
            "    \"is_creator_requesting_mashup\": \"0\",\n" +
            "    \"additional_audio_info\": {\n" +
            "        \"has_voiceover_attribution\": \"0\"\n" +
            "    },\n" +
            "    \"device\": {\n" +
            "        \"manufacturer\": \"Samsung\",\n" +
            "        \"model\": \"Samsung Galaxy\",\n" +
            "        \"android_version\": 29,\n" +
            "        \"android_release\": \"10\"\n" +
            "    },\n" +
            "    \"mashup_info\": {\n" +
            "        \"original_media_id\": \"AORIGPOSTMEDIAID\",\n" +
            "        \"original_media_duration\": 8267,\n" +
            "        \"original_media_is_shared_to_facebook\": false,\n" +
            "        \"are_remixes_crosspostable\": false,\n" +
            "        \"source_media_creation_state\": \"SEQUENTIAL_REMIX\",\n" +
            "        \"original_media_is_photo\": false,\n" +
            "        \"mashup_type\": \"sequential\"\n" +
            "    },\n" +
            "    \"length\": 8.521,\n" +
            "    \"clips\": [\n" +
            "        {\n" +
            "            \"length\": 8.521,\n" +
            "            \"source_type\": \"3\",\n" +
            "            \"camera_position\": \"back\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"extra\": {\n" +
            "        \"source_width\": 1080,\n" +
            "        \"source_height\": 1920\n" +
            "    },\n" +
            "    \"audio_muted\": false,\n" +
            "    \"poster_frame_index\": 0,\n" +
            "    \"clips_segments_metadata\": {\n" +
            "        \"num_segments\": 2,\n" +
            "        \"clips_segments\": [\n" +
            "            {\n" +
            "                \"index\": 0,\n" +
            "                \"face_effect_id\": null,\n" +
            "                \"speed\": 100,\n" +
            "                \"source_type\": \"0\",\n" +
            "                \"duration_ms\": 8335,\n" +
            "                \"audio_type\": \"music_selection\",\n" +
            "                \"from_draft\": \"0\",\n" +
            "                \"camera_position\": -1,\n" +
            "                \"media_folder\": null,\n" +
            "                \"media_type\": \"video\",\n" +
            "                \"original_media_type\": 2\n" +
            "            },\n" +
            "            {\n" +
            "                \"index\": 1,\n" +
            "                \"face_effect_id\": null,\n" +
            "                \"speed\": 100,\n" +
            "                \"source_type\": \"1\",\n" +
            "                \"duration_ms\": 186,\n" +
            "                \"audio_type\": \"music_selection\",\n" +
            "                \"from_draft\": \"0\",\n" +
            "                \"camera_position\": 1,\n" +
            "                \"media_folder\": null,\n" +
            "                \"media_type\": \"video\",\n" +
            "                \"original_media_type\": 2\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"clips_audio_metadata\": {\n" +
            "        \"original\": {\n" +
            "            \"volume_level\": 1.0\n" +
            "        },\n" +
            "        \"song\": {\n" +
            "            \"volume_level\": 1.0,\n" +
            "            \"is_saved\": \"0\",\n" +
            "            \"artist_name\": \"AAUDIOARTISTNAME\",\n" +
            "            \"audio_asset_id\": \"AAUDIOASSETID\",\n" +
            "            \"audio_cluster_id\": \"AAUDIOCLUSTERID\",\n" +
            "            \"track_name\": \"AORIGINALAUDIOTITLE\",\n" +
            "            \"is_picked_precapture\": \"1\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"music_params\": {\n" +
            "        \"audio_asset_id\": \"AAUDIOASSETID\",\n" +
            "        \"audio_cluster_id\": \"AAUDIOCLUSTERID\",\n" +
            "        \"audio_asset_start_time_in_ms\": AUDIOASSETSTARTTIMEMS,\n" +
            "        \"derived_content_start_time_in_ms\": 0,\n" +
            "        \"overlap_duration_in_ms\": 15000,\n" +
            "        \"browse_session_id\": \"a9304722-4734-4675-b1d3-90296712371c\",\n" +
            "        \"product\": \"story_camera_clips_v2\",\n" +
            "        \"song_name\": \"AORIGINALAUDIOTITLE\",\n" +
            "        \"artist_name\": \"AAUDIOARTISTNAME\",\n" +
            "        \"alacorn_session_id\": null\n" +
            "    }\n" +
            "}";
}

