package in.semibit.media.common.igclientext.post;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class RemixPayload {
    public static String getRemixPayload(MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload payload){
        JSONObject tmj = new JSONObject();
        try {
            String process = template
                    .replaceAll("SOUND_ORIGINAL_MEDIA_ID", payload.originalMediaId);

            tmj = new JSONObject(process);


            tmj.put("_csrftoken",payload.get_csrftoken());
            tmj.put("_uid",payload.get_uid());
            tmj.put("_uuid",payload.get_uuid());
            tmj.put("device_id",payload.getDevice_id());
            tmj.put("guid",payload.getGuid());
            tmj.put("phone_id",payload.getPhone_id());
            tmj.put("caption",payload.caption());
            tmj.put("upload_id",payload.upload_id());

            tmj.put("camera_session_id", UUID.randomUUID());



        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tmj.toString();
    }
    static String template ="{\n" +
            "    \"clips_share_preview_to_feed\": \"1\",\n" +
            "    \"is_shared_to_fb\": \"0\",\n" +
            "    \"is_clips_edited\": \"0\",\n" +
            "    \"like_and_view_counts_disabled\": \"0\",\n" +
            "    \"camera_entry_point\": \"299\",\n" +
            "    \"is_created_with_sound_sync\": \"0\",\n" +
            "    \"filter_type\": \"0\",\n" +
            "    \"camera_session_id\": \"5dc1c2db-6e62-40b3-98d3-9281dfbf8f5c\",\n" +
            "    \"disable_comments\": \"0\",\n" +
            "    \"clips_creation_entry_point\": \"clips\",\n" +
            "    \"timezone_offset\": \"19800\",\n" +
            "    \"source_type\": \"3\",\n" +
            "    \"camera_position\": \"unknown\",\n" +
            "    \"video_result\": \"\",\n" +
            "    \"is_created_with_contextual_music_recs\": \"0\",\n" +
            "    \"_uid\": \"51132554072\",\n" +
            "    \"device_id\": \"android-ba9156177f99d2ee\",\n" +
            "    \"_uuid\": \"7397b647-0663-4d02-9746-8cd93c61e6f1\",\n" +
            "    \"nav_chain\": \"ExploreFragment:explore_popular:3:main_search::,SingleSearchTypeaheadTabFragment:search_typeahead:4:button::,UserDetailFragment:profile:5:search_result::,TRUNCATEDx4,IgCameraViewController:reel_composer_camera:48:button::,ClipsCameraFragment:clips_precapture_camera:49:button::,VideoViewController:clips_postcapture_camera:50:button::,ClipsShareSheetFragment:clips_share_sheet:51:button::,ClipsCoverPhotoPickerFragment:clips_cover_photo_picker_fragment:52:button::,ClipsShareSheetFragment:clips_share_sheet:53:button::,ClipsShareSheetFragment:clips_share_sheet:54:button::\",\n" +
            "    \"caption\": \"MY CAPTION\",\n" +
            "    \"video_subtitles_enabled\": \"1\",\n" +
            "    \"capture_type\": \"clips_v2\",\n" +
            "    \"audience\": \"default\",\n" +
            "    \"upload_id\": \"12802067644500\",\n" +
            "    \"template_clips_media_id\": \"null\",\n" +
            "    \"is_creator_requesting_mashup\": \"0\",\n" +
            "    \"additional_audio_info\": {\n" +
            "        \"has_voiceover_attribution\": \"0\"\n" +
            "    },\n" +
//            "    \"mashup_info\": {\n" +
//            "        \"original_media_id\": \"2926971262238630058\",\n" +
//            "        \"original_media_duration\": 26959,\n" +
//            "        \"original_media_is_shared_to_facebook\": true,\n" +
//            "        \"are_remixes_crosspostable\": false,\n" +
//            "        \"source_media_creation_state\": \"SEQUENTIAL_REMIX\",\n" +
//            "        \"original_media_is_photo\": false,\n" +
//            "        \"mashup_type\": \"sequential\"\n" +
//            "    },\n" +
            " \"tap_models\": \"[{\\\"x\\\":0.0,\\\"y\\\":0.0,\\\"z\\\":0,\\\"width\\\":0.0,\\\"height\\\":0.0,\\\"rotation\\\":0.0,\\\"type\\\":\\\"music\\\",\\\"tag\\\":\\\"8003c09b-aef5-492b-b8f3-70c262dbe777\\\",\\\"audio_asset_start_time_in_ms\\\":0,\\\"audio_asset_suggested_start_time_in_ms\\\":0,\\\"derived_content_start_time_in_ms\\\":0,\\\"overlap_duration_in_ms\\\":15000,\\\"browse_session_id\\\":\\\"ecde0729-aec3-4894-b6f8-cf2277d54636\\\",\\\"music_product\\\":\\\"clips_camera_format_v2\\\",\\\"audio_asset_id\\\":\\\"5367993556626807\\\",\\\"progressive_download_url\\\":\\\"https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/296494780_459011852779921_8135167926860593150_n.m4a?_nc_cat=1&ccb=1-7&_nc_sid=02c1ff&_nc_ohc=qR6YBezW1RoAX82s8xx&_nc_ad=z-m&_nc_cid=0&_nc_ht=scontent-maa2-1.xx&oh=00_AT9oOpMXxWAlbI0qJU-0KJT8_7PSW8Q80ZXTTppAL8jjsQ&oe=6328C0D9\\\",\\\"duration_in_ms\\\":38705,\\\"dash_manifest\\\":\\\"<?xml version=\\\\\\\"1.0\\\\\\\" encoding=\\\\\\\"UTF-8\\\\\\\"?>\\\\n<!--Generated with https://github.com/google/shaka-packager version v1.6.0-release-->\\\\n<MPD xmlns=\\\\\\\"urn:mpeg:dash:schema:mpd:2011\\\\\\\" xmlns:xsi=\\\\\\\"http://www.w3.org/2001/XMLSchema-instance\\\\\\\" xmlns:xlink=\\\\\\\"http://www.w3.org/1999/xlink\\\\\\\" xmlns:cenc=\\\\\\\"urn:mpeg:cenc:2013\\\\\\\" xsi:schemaLocation=\\\\\\\"urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd\\\\\\\" profiles=\\\\\\\"urn:mpeg:dash:profile:isoff-on-demand:2011\\\\\\\" minBufferTime=\\\\\\\"PT2S\\\\\\\" type=\\\\\\\"static\\\\\\\" mediaPresentationDuration=\\\\\\\"PT38.7048S\\\\\\\">\\\\n  <Period id=\\\\\\\"0\\\\\\\">\\\\n    <AdaptationSet id=\\\\\\\"0\\\\\\\" contentType=\\\\\\\"audio\\\\\\\" subsegmentAlignment=\\\\\\\"true\\\\\\\">\\\\n      <Representation id=\\\\\\\"0\\\\\\\" bandwidth=\\\\\\\"82939\\\\\\\" codecs=\\\\\\\"mp4a.40.2\\\\\\\" mimeType=\\\\\\\"audio/mp4\\\\\\\" audioSamplingRate=\\\\\\\"22050\\\\\\\">\\\\n        <AudioChannelConfiguration schemeIdUri=\\\\\\\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\\\\\\\" value=\\\\\\\"2\\\\\\\"/>\\\\n        <BaseURL>https://scontent-maa2-1.xx.fbcdn.net/v/t39.12897-6/296472500_615342929926585_1459296909370843488_n.m4a?_nc_cat=1&amp;ccb=1-7&amp;_nc_sid=02c1ff&amp;_nc_ohc=pofrZ-ueJ8EAX9gR-ST&amp;_nc_oc=AQn6goWe_82AoEDCy-sHg1O90dZzUIXuEADPjI9vbJgM5BUJ9o3vtEZpTDqZbw9B1K0&amp;_nc_ad=z-m&amp;_nc_cid=0&amp;_nc_ht=scontent-maa2-1.xx&amp;oh=00_AT-Qiq5h0NqY9DuT4H2uTQSffG6IMVRYKnrXYeqqWgWoqA&amp;oe=63275B25</BaseURL>\\\\n        <SegmentBase indexRange=\\\\\\\"743-1002\\\\\\\" timescale=\\\\\\\"44100\\\\\\\">\\\\n          <Initialization range=\\\\\\\"0-742\\\\\\\"/>\\\\n        </SegmentBase>\\\\n      </Representation>\\\\n    </AdaptationSet>\\\\n  </Period>\\\\n</MPD>\\\\n\\\",\\\"title\\\":\\\"Dekha Ek Khwaab x Laila ~ Anna x SushYohan Mashup\\\",\\\"display_artist\\\":\\\"oyeeditorranna\\\",\\\"cover_artwork_uri\\\":\\\"https://scontent-maa2-1.cdninstagram.com/v/t51.2885-19/306520109_674321106873824_1623811468163544356_n.jpg?stp=dst-jpg_s150x150&efg=eyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ&_nc_ht=scontent-maa2-1.cdninstagram.com&_nc_cat=1&_nc_ohc=9akSwvkHyX8AX_iztC9&edm=ACoBdB8BAAAA&ccb=7-5&oh=00_AT_ImO78klSOxgUBNg_432cXXWXdBLuEHOMG4vq7a62RZw&oe=632A5DF4&_nc_sid=046a24\\\",\\\"cover_artwork_thumbnail_uri\\\":\\\"https://scontent-maa2-1.cdninstagram.com/v/t51.2885-19/306520109_674321106873824_1623811468163544356_n.jpg?stp=dst-jpg_s150x150&efg=eyJybWQiOiJpZ19hbmRyb2lkX21vYmlsZV9uZXR3b3JrX3N0YWNrX3Bvd2VyX3N0YXRlX3FwbF9hbm5vdGF0aW9uc18zOmNvbnRyb2wifQ&_nc_ht=scontent-maa2-1.cdninstagram.com&_nc_cat=1&_nc_ohc=9akSwvkHyX8AX_iztC9&edm=ACoBdB8BAAAA&ccb=7-5&oh=00_AT_ImO78klSOxgUBNg_432cXXWXdBLuEHOMG4vq7a62RZw&oe=632A5DF4&_nc_sid=046a24\\\",\\\"is_explicit\\\":false,\\\"has_lyrics\\\":false,\\\"is_original_sound\\\":true,\\\"is_local_audio\\\":false,\\\"allows_saving\\\":false,\\\"original_media_id\\\":\\\"SOUND_ORIGINAL_MEDIA_ID\\\",\\\"hide_remixing\\\":false,\\\"picked_in_post_capture\\\":false,\\\"is_bookmarked\\\":false,\\\"should_mute_audio\\\":false,\\\"product\\\":\\\"story_camera_clips_v2\\\",\\\"is_sticker\\\":false,\\\"display_type\\\":\\\"HIDDEN\\\",\\\"tap_state\\\":0,\\\"tap_state_str_id\\\":\\\"\\\"}]\",\n" +
            "    "+
//            "    \"length\": ${DURSEC},\n" +
//            "    \"clips\": [\n" +
//            "        {\n" +
//            "            \"length\": ${DURSEC},\n" +
//            "            \"source_type\": \"3\",\n" +
//            "            \"camera_position\": \"back\"\n" +
//            "        }\n" +
//            "    ],\n" +
//            "    \"extra\": {\n" +
//            "        \"source_width\": ${DIM_W},\n" +
//            "        \"source_height\": ${DIM_H}\n" +
//            "    },\n" +
            "    \"remixed_original_sound_params\": {\n" +
            "        \"original_media_id\": \"SOUND_ORIGINAL_MEDIA_ID\"\n" +
            "    }\n" +
//            "   , \"audio_muted\": false,\n" +
//            "    \"poster_frame_index\": 87,\n" +
//            "    \"clips_segments_metadata\": {\n" +
//            "        \"num_segments\": 2,\n" +
//            "        \"clips_segments\": [\n" +
//            "            {\n" +
//            "                \"index\": 0,\n" +
//            "                \"face_effect_id\": null,\n" +
//            "                \"speed\": 100,\n" +
//            "                \"source_type\": \"0\",\n" +
//            "                \"duration_ms\": ${DUR0MS},\n" +
//            "                \"audio_type\": \"original_remix\",\n" +
//            "                \"from_draft\": \"0\",\n" +
//            "                \"camera_position\": -1,\n" +
//            "                \"media_folder\": null,\n" +
//            "                \"media_type\": \"video\",\n" +
//            "                \"original_media_type\": 2\n" +
//            "            },\n" +
//            "            {\n" +
//            "                \"index\": 1,\n" +
//            "                \"face_effect_id\": null,\n" +
//            "                \"speed\": 100,\n" +
//            "                \"source_type\": \"1\",\n" +
//            "                \"duration_ms\": ${DUR1MS},\n" +
//            "                \"audio_type\": \"original_remix\",\n" +
//            "                \"from_draft\": \"0\",\n" +
//            "                \"camera_position\": 1,\n" +
//            "                \"media_folder\": null,\n" +
//            "                \"media_type\": \"video\",\n" +
//            "                \"original_media_type\": 2\n" +
//            "            }\n" +
//            "        ]\n" +
//            "    },\n" +
//            "    \"clips_audio_metadata\": {\n" +
//            "        \"original\": {\n" +
//            "            \"volume_level\": 1.0\n" +
//            "        },\n" +
//            "        \"remix\": {\n" +
//            "            \"volume_level\": 1.0,\n" +
//            "            \"is_saved\": \"0\",\n" +
//            "            \"artist_name\": \"oyeeditorranna\",\n" +
//            "            \"audio_asset_id\": \"5367993556626807\",\n" +
//            "            \"audio_cluster_id\": null,\n" +
//            "            \"track_name\": \"Dekha Ek Khwaab x Laila ~ Anna x SushYohan Mashup\",\n" +
//            "            \"is_picked_precapture\": \"1\",\n" +
//            "            \"original_media_id\": \"SOUND_ORIGINAL_MEDIA_ID\"\n" +
//            "        }\n" +
//            "    }\n" +
            "}";
}
