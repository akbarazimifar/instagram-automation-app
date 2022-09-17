package in.semibit.media.postbot.poc;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.google.gson.internal.LinkedTreeMap;
import com.semibit.ezandroidutils.EzUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.igclientext.post.MediaConfigureToClipsRequestExt;
import in.semibit.media.common.igclientext.post.model.PostItem;
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
            } else if (postItem.getClipsMetadata().getMusicInfo() != null) {
                LinkedTreeMap musicInfoMap = (LinkedTreeMap) postItem.getClipsMetadata().getMusicInfo();
                LinkedTreeMap musicAssetInfoMap = (LinkedTreeMap) musicInfoMap.get("music_asset_info");
                audioAssetId = musicAssetInfoMap.get("audio_asset_id").toString();
                audioArtistUserName = musicAssetInfoMap.get("ig_username").toString();
                audioTitle = musicAssetInfoMap.get("title").toString();
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
            String s_2906686555970937661 = remixOfPostMediaId;//"" + postItem.getClipsMetadata().getOriginalSoundInfo().getOriginalMediaId();
            String s_347624030914642 = audioAssetId;//"" + postItem.getClipsMetadata().getOriginalSoundInfo().getAudioAssetId();
            String soundArtistUserName = audioArtistUserName;// postItem.getClipsMetadata().getOriginalSoundInfo().getIgArtist().getUsername();

            OkHttpClient okHttpClient = igClient.getHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
            String bodyString = "signed_body=SIGNATURE.%7B%22clips_share_preview_to_feed%22%3A%221%22%2C%22is_shared_to_fb%22%3A%220%22%2C%22is_clips_edited%22%3A%220%22%2C%22like_and_view_counts_disabled%22%3A%220%22%2C%22camera_entry_point%22%3A%22299%22%2C%22tap_models%22%3A%22%5B%7B%5C%22x%5C%22%3A0.0%2C%5C%22y%5C%22%3A0.0%2C%5C%22z%5C%22%3A0%2C%5C%22width%5C%22%3A0.0%2C%5C%22height%5C%22%3A0.0%2C%5C%22rotation%5C%22%3A0.0%2C%5C%22type%5C%22%3A%5C%22music%5C%22%2C%5C%22tag%5C%22%3A%5C%22386fc201-6d73-43ad-83a4-a3291ccf5488%5C%22%2C%5C%22audio_asset_start_time_in_ms%5C%22%3A0%2C%5C%22audio_asset_suggested_start_time_in_ms%5C%22%3A0%2C%5C%22derived_content_start_time_in_ms%5C%22%3A0%2C%5C%22overlap_duration_in_ms%5C%22%3A15000%2C%5C%22browse_session_id%5C%22%3A%5C%22d46be47e-3b30-479d-9403-166e1af80caf%5C%22%2C%5C%22music_product%5C%22%3A%5C%22clips_camera_format_v2%5C%22%2C%5C%22audio_asset_id%5C%22%3A%5C%22347624030914642%5C%22%2C%5C%22progressive_download_url%5C%22%3A%5C%22https%3A%2F%2Fscontent-maa2-1.xx.fbcdn.net%2Fv%2Ft39.12897-6%2F299997302_1479800132448155_5706075520205073543_n.m4a%3F_nc_cat%3D106%26ccb%3D1-7%26_nc_sid%3D02c1ff%26_nc_ohc%3DzHeGZvfYiKwAX8nMjJK%26_nc_ad%3Dz-m%26_nc_cid%3D0%26_nc_ht%3Dscontent-maa2-1.xx%26oh%3D00_AT-XviAjHpxHaf_VMd5VImEFTkxDvEXE3sB6sWrSg5nUsg%26oe%3D6329E320%5C%22%2C%5C%22duration_in_ms%5C%22%3A28026%2C%5C%22dash_manifest%5C%22%3A%5C%22%3C%3Fxml+version%3D%5C%5C%5C%221.0%5C%5C%5C%22+encoding%3D%5C%5C%5C%22UTF-8%5C%5C%5C%22%3F%3E%5C%5Cn%3C%21--Generated+with+https%3A%2F%2Fgithub.com%2Fgoogle%2Fshaka-packager+version+v1.6.0-release--%3E%5C%5Cn%3CMPD+xmlns%3D%5C%5C%5C%22urn%3Ampeg%3Adash%3Aschema%3Ampd%3A2011%5C%5C%5C%22+xmlns%3Axsi%3D%5C%5C%5C%22http%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema-instance%5C%5C%5C%22+xmlns%3Axlink%3D%5C%5C%5C%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%5C%5C%5C%22+xmlns%3Acenc%3D%5C%5C%5C%22urn%3Ampeg%3Acenc%3A2013%5C%5C%5C%22+xsi%3AschemaLocation%3D%5C%5C%5C%22urn%3Ampeg%3Adash%3Aschema%3Ampd%3A2011+DASH-MPD.xsd%5C%5C%5C%22+profiles%3D%5C%5C%5C%22urn%3Ampeg%3Adash%3Aprofile%3Aisoff-on-demand%3A2011%5C%5C%5C%22+minBufferTime%3D%5C%5C%5C%22PT2S%5C%5C%5C%22+type%3D%5C%5C%5C%22static%5C%5C%5C%22+mediaPresentationDuration%3D%5C%5C%5C%22PT28.025S%5C%5C%5C%22%3E%5C%5Cn++%3CPeriod+id%3D%5C%5C%5C%220%5C%5C%5C%22%3E%5C%5Cn++++%3CAdaptationSet+id%3D%5C%5C%5C%220%5C%5C%5C%22+contentType%3D%5C%5C%5C%22audio%5C%5C%5C%22+subsegmentAlignment%3D%5C%5C%5C%22true%5C%5C%5C%22%3E%5C%5Cn++++++%3CRepresentation+id%3D%5C%5C%5C%220%5C%5C%5C%22+bandwidth%3D%5C%5C%5C%2270239%5C%5C%5C%22+codecs%3D%5C%5C%5C%22mp4a.40.2%5C%5C%5C%22+mimeType%3D%5C%5C%5C%22audio%2Fmp4%5C%5C%5C%22+audioSamplingRate%3D%5C%5C%5C%2222050%5C%5C%5C%22%3E%5C%5Cn++++++++%3CAudioChannelConfiguration+schemeIdUri%3D%5C%5C%5C%22urn%3Ampeg%3Adash%3A23003%3A3%3Aaudio_channel_configuration%3A2011%5C%5C%5C%22+value%3D%5C%5C%5C%222%5C%5C%5C%22%2F%3E%5C%5Cn++++++++%3CBaseURL%3Ehttps%3A%2F%2Fscontent-maa2-1.xx.fbcdn.net%2Fv%2Ft39.12897-6%2F300112693_1369990176858319_7851170966804425913_n.m4a%3F_nc_cat%3D100%26amp%3Bccb%3D1-7%26amp%3B_nc_sid%3D02c1ff%26amp%3B_nc_ohc%3DztGF8NvwPM0AX8Qecel%26amp%3B_nc_ad%3Dz-m%26amp%3B_nc_cid%3D0%26amp%3B_nc_ht%3Dscontent-maa2-1.xx%26amp%3Boh%3D00_AT-KYeTPtrqlaTT72PM4s9GPmTsSIYXNLzNRx9bz1u5SzA%26amp%3Boe%3D6329985D%3C%2FBaseURL%3E%5C%5Cn++++++++%3CSegmentBase+indexRange%3D%5C%5C%5C%22743-942%5C%5C%5C%22+timescale%3D%5C%5C%5C%2244100%5C%5C%5C%22%3E%5C%5Cn++++++++++%3CInitialization+range%3D%5C%5C%5C%220-742%5C%5C%5C%22%2F%3E%5C%5Cn++++++++%3C%2FSegmentBase%3E%5C%5Cn++++++%3C%2FRepresentation%3E%5C%5Cn++++%3C%2FAdaptationSet%3E%5C%5Cn++%3C%2FPeriod%3E%5C%5Cn%3C%2FMPD%3E%5C%5Cn%5C%22%2C%5C%22title%5C%22%3A%5C%22Originalaudio%5C%22%2C%5C%22display_artist%5C%22%3A%5C%22not_your_type_yt%5C%22%2C%5C%22cover_artwork_uri%5C%22%3A%5C%22https%3A%2F%2Fscontent-maa2-2.cdninstagram.com%2Fv%2Ft51.2885-19%2F272657676_2469488359849327_6306960314298840502_n.jpg%3Fstp%3Ddst-jpg_s150x150%26efg%3DeyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ%26_nc_ht%3Dscontent-maa2-2.cdninstagram.com%26_nc_cat%3D107%26_nc_ohc%3DpxhM5guzEowAX-Vrx6j%26edm%3DAOM4IUYBAAAA%26ccb%3D7-5%26oh%3D00_AT_t30Y8FMomzq6DrVqIj428BWXreiqt0sR0MyH-FycpjQ%26oe%3D632AFEBF%26_nc_sid%3D734323%5C%22%2C%5C%22cover_artwork_thumbnail_uri%5C%22%3A%5C%22https%3A%2F%2Fscontent-maa2-2.cdninstagram.com%2Fv%2Ft51.2885-19%2F272657676_2469488359849327_6306960314298840502_n.jpg%3Fstp%3Ddst-jpg_s150x150%26efg%3DeyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ%26_nc_ht%3Dscontent-maa2-2.cdninstagram.com%26_nc_cat%3D107%26_nc_ohc%3DpxhM5guzEowAX-Vrx6j%26edm%3DAOM4IUYBAAAA%26ccb%3D7-5%26oh%3D00_AT_t30Y8FMomzq6DrVqIj428BWXreiqt0sR0MyH-FycpjQ%26oe%3D632AFEBF%26_nc_sid%3D734323%5C%22%2C%5C%22is_explicit%5C%22%3Afalse%2C%5C%22has_lyrics%5C%22%3Afalse%2C%5C%22is_original_sound%5C%22%3Atrue%2C%5C%22is_local_audio%5C%22%3Afalse%2C%5C%22allows_saving%5C%22%3Afalse%2C%5C%22original_media_id%5C%22%3A%5C%222906686555970937661%5C%22%2C%5C%22hide_remixing%5C%22%3Afalse%2C%5C%22picked_in_post_capture%5C%22%3Afalse%2C%5C%22is_bookmarked%5C%22%3Afalse%2C%5C%22should_mute_audio%5C%22%3Afalse%2C%5C%22product%5C%22%3A%5C%22story_camera_clips_v2%5C%22%2C%5C%22is_sticker%5C%22%3Afalse%2C%5C%22display_type%5C%22%3A%5C%22HIDDEN%5C%22%2C%5C%22tap_state%5C%22%3A0%2C%5C%22tap_state_str_id%5C%22%3A%5C%22%5C%22%7D%5D%22%2C%22is_created_with_sound_sync%22%3A%220%22%2C%22filter_type%22%3A%220%22%2C%22camera_session_id%22%3A%220ddef447-b2de-426b-9b41-28a9c943a74e%22%2C%22disable_comments%22%3A%220%22%2C%22clips_creation_entry_point%22%3A%22clips%22%2C%22timezone_offset%22%3A%2219800%22%2C%22source_type%22%3A%223%22%2C%22camera_position%22%3A%22unknown%22%2C%22video_result%22%3A%22%22%2C%22is_created_with_contextual_music_recs%22%3A%220%22%2C%22_uid%22%3A%2251132554072%22%2C%22device_id%22%3A%22android-ba9156177f99d2ee%22%2C%22_uuid%22%3A%227397b647-0663-4d02-9746-8cd93c61e6f1%22%2C%22nav_chain%22%3A%22SelfFragment%3Aself_profile%3A2%3Amain_profile%3A%3A%2CContextualFeedFragment%3Afeed_contextual_self_profile%3A5%3Abutton%3A%3A%2CClipsViewerFragment%3Aclips_viewer_feed_contextual_self_profile%3A6%3Afeed_contextual_self_profile%3A%3A%2CTRUNCATEDx1%2CClipsRemixOptionsFragment%3Aclips_remix_options%3A8%3Abutton%3A%3A%2CClipsViewerFragment%3Aclips_viewer_original_creator_video%3A9%3Abutton%3A%3A%2CClipsRemixOptionsFragment%3Aclips_remix_options%3A10%3Abutton%3A%3A%2CIgCameraViewController%3Areel_composer_camera%3A11%3Abutton%3A%3A%2CClipsCameraFragment%3Aclips_precapture_camera%3A12%3Abutton%3A%3A%2CVideoViewController%3Aclips_postcapture_camera%3A13%3Abutton%3A%3A%2CClipsShareSheetFragment%3Aclips_share_sheet%3A14%3Abutton%3A%3A%22%2C%22caption%22%3A%22ManualRemix%22%2C%22video_subtitles_enabled%22%3A%221%22%2C%22capture_type%22%3A%22clips_v2%22%2C%22audience%22%3A%22default%22%2C%22upload_id%22%3A%22137624447800%22%2C%22template_clips_media_id%22%3A%22null%22%2C%22is_creator_requesting_mashup%22%3A%220%22%2C%22additional_audio_info%22%3A%7B%22has_voiceover_attribution%22%3A%220%22%7D%2C%22device%22%3A%7B%22manufacturer%22%3A%22Google%22%2C%22model%22%3A%22Android+SDK+built+for+x86%22%2C%22android_version%22%3A29%2C%22android_release%22%3A%2210%22%7D%2C%22mashup_info%22%3A%7B%22original_media_id%22%3A%222906686555970937661%22%2C%22original_media_duration%22%3A27957%2C%22original_media_is_shared_to_facebook%22%3Afalse%2C%22are_remixes_crosspostable%22%3Afalse%2C%22source_media_creation_state%22%3A%22SEQUENTIAL_REMIX%22%2C%22original_media_is_photo%22%3Afalse%2C%22mashup_type%22%3A%22sequential%22%7D%2C%22length%22%3A28.118%2C%22clips%22%3A%5B%7B%22length%22%3A28.118%2C%22source_type%22%3A%223%22%2C%22camera_position%22%3A%22back%22%7D%5D%2C%22extra%22%3A%7B%22source_width%22%3A1080%2C%22source_height%22%3A1920%7D%2C%22remixed_original_sound_params%22%3A%7B%22original_media_id%22%3A%222906686555970937661%22%7D%2C%22audio_muted%22%3Afalse%2C%22poster_frame_index%22%3A0%2C%22clips_segments_metadata%22%3A%7B%22num_segments%22%3A2%2C%22clips_segments%22%3A%5B%7B%22index%22%3A0%2C%22face_effect_id%22%3Anull%2C%22speed%22%3A100%2C%22source_type%22%3A%220%22%2C%22duration_ms%22%3A28025%2C%22audio_type%22%3A%22original_remix%22%2C%22from_draft%22%3A%220%22%2C%22camera_position%22%3A-1%2C%22media_folder%22%3Anull%2C%22media_type%22%3A%22video%22%2C%22original_media_type%22%3A2%7D%2C%7B%22index%22%3A1%2C%22face_effect_id%22%3Anull%2C%22speed%22%3A100%2C%22source_type%22%3A%221%22%2C%22duration_ms%22%3A93%2C%22audio_type%22%3A%22original_remix%22%2C%22from_draft%22%3A%220%22%2C%22camera_position%22%3A1%2C%22media_folder%22%3Anull%2C%22media_type%22%3A%22video%22%2C%22original_media_type%22%3A2%7D%5D%7D%2C%22clips_audio_metadata%22%3A%7B%22original%22%3A%7B%22volume_level%22%3A1.0%7D%2C%22remix%22%3A%7B%22volume_level%22%3A1.0%2C%22is_saved%22%3A%220%22%2C%22artist_name%22%3A%22not_your_type_yt%22%2C%22audio_asset_id%22%3A%22347624030914642%22%2C%22audio_cluster_id%22%3Anull%2C%22track_name%22%3A%22Original+audio%22%2C%22is_picked_precapture%22%3A%221%22%2C%22original_media_id%22%3A%222906686555970937661%22%7D%7D%7D";


            bodyString = bodyString.replaceAll("2906686555970937661", s_2906686555970937661);
            bodyString = bodyString.replaceAll("347624030914642", s_347624030914642);
            bodyString = bodyString.replaceAll("137624447800", upload_id);
            bodyString = bodyString.replaceAll(getUserId(), getUserId());
            bodyString = bodyString.replaceAll("android-ba9156177f99d2ee", igClient.getDeviceId());
            bodyString = bodyString.replaceAll("7397b647-0663-4d02-9746-8cd93c61e6f1", UUID.randomUUID().toString());
            bodyString = bodyString.replaceAll("not_your_type_yt", soundArtistUserName);
            bodyString = bodyString.replaceAll("ManualRemix", URLEncoder.encode(caption, "UTF-8"));
            bodyString = bodyString.replaceAll("Originalaudio", URLEncoder.encode(audioTitle, "UTF-8"));


            RequestBody body = RequestBody.create(mediaType, bodyString);

            Request request = new Request.Builder()
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
//                .addHeader("X-Ig-Device-Id", "7397b647-0663-4d02-9746-8cd93c61e6f1")
//                .addHeader("X-Ig-Family-Device-Id", "f4a03c5e-a471-4c32-b97c-9d6f2995fcf8")
//                .addHeader("X-Ig-Android-Id", "android-ba9156177f99d2ee")
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
                    .addHeader("X-Mid", "YyImQwABAAHTYa3KnckXVbkVIu87")
                    .addHeader("Ig-U-Ig-Direct-Region-Hint", "ASH," + getUserId() + ",1694884196:01f76ac04d49354aade2c1ceca113231926e6917cb8322307c1424cedec080f5894dc0dc")
                    .addHeader("Ig-U-Shbid", "15628," + getUserId() + ",1694718419:01f7c629116c0471f0b16e94af3908231b4315e64199848a706e936feea04805efbe7b29")
                    .addHeader("Ig-U-Shbts", "1663182419," + getUserId() + ",1694718419:01f7391ecb88b7b7f31a5e39bf0f9d7d77835aaa77df3703e08d5f25af21a8721ca059da")
                    .addHeader("Ig-U-Ds-User-Id", getUserId())
                    .addHeader("Ig-U-Rur", "EAG," + getUserId() + ",1694885399:01f79f261246f155e3834c8515adb93ae55a03b1370c4933546f5625a588886f6c799b25")
                    .addHeader("Ig-Intended-User-Id", getUserId())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .addHeader("Content-Length", String.valueOf(body.contentLength()))
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("X-Fb-Http-Engine", "Liger")
                    .addHeader("X-Fb-Client-Ip", "True")
                    .addHeader("X-Fb-Server-Cluster", "True")
//                .addHeader("Authorization", "Bearer IGT:2:eyJkc191c2VyX2lkIjoiNTExMzI1NTQwNzIiLCJzZXNzaW9uaWQiOiI1MTEzMjU1NDA3MiUzQWxWMTlnSmlsS040S01nJTNBMTMlM0FBWWZhME9LRnVicjlSMUxnVlZ1SGF4aDBxWm5pUGtlT29QZy01UWNBSkEifQ==")
//                .addHeader("Cookie", "csrftoken=IZ4NEVRyURDHfXRMP5mppc1JwOen2jcz; ig_did=20177F9C-229D-414A-B3EB-FCD09087F5D2; ig_nrcb=1; mid=YwpBwgALAAFZj_GeFM3yHd5Q2oRR")


                    .addHeader("Ig-U-Ds-User-Id", getUserId())
                    .addHeader("Ig-Intended-User-Id", getUserId())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept-Encoding", "gzip, deflate")
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

                    .build();
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
            res.length();
            LogsViewModel.addToLog("REEL CONFIGURE RESPONSE " + res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            LogsViewModel.addToLog("REEL CONFIGURE ERROR " + e.getMessage());

        }
        return null;
    }
}

