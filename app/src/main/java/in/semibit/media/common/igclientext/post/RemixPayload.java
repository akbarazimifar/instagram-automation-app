package in.semibit.media.common.igclientext.post;

import org.json.JSONException;
import org.json.JSONObject;

public class RemixPayload {
    public static String getRemixPayload(MediaConfigureReelRemixRequest.MediaConfigurePayload payload){
        JSONObject tmj = new JSONObject();
        try {
            tmj = new JSONObject(template);


            tmj.put("_csrftoken",payload.get_csrftoken());
            tmj.put("_uid",payload.get_uid());
            tmj.put("_uuid",payload.get_uuid());
            tmj.put("device_id",payload.getDevice_id());
            tmj.put("guid",payload.getGuid());
            tmj.put("phone_id",payload.getPhone_id());
            tmj.put("caption",payload.caption());
            tmj.put("upload_id",payload.upload_id());
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
            "    \"device\": {\n" +
            "        \"manufacturer\": \"Google\",\n" +
            "        \"model\": \"Android SDK built for x86\",\n" +
            "        \"android_version\": 29,\n" +
            "        \"android_release\": \"10\"\n" +
            "    },\n" +
            "    \"mashup_info\": {\n" +
            "        \"original_media_id\": \"2926971262238630058\",\n" +
            "        \"original_media_duration\": 26959,\n" +
            "        \"original_media_is_shared_to_facebook\": true,\n" +
            "        \"are_remixes_crosspostable\": false,\n" +
            "        \"source_media_creation_state\": \"SEQUENTIAL_REMIX\",\n" +
            "        \"original_media_is_photo\": false,\n" +
            "        \"mashup_type\": \"sequential\"\n" +
            "    },\n" +
            "    \"length\": 27.536,\n" +
            "    \"clips\": [\n" +
            "        {\n" +
            "            \"length\": 27.536,\n" +
            "            \"source_type\": \"3\",\n" +
            "            \"camera_position\": \"back\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"extra\": {\n" +
            "        \"source_width\": 1080,\n" +
            "        \"source_height\": 1920\n" +
            "    },\n" +
            "    \"remixed_original_sound_params\": {\n" +
            "        \"original_media_id\": \"2895955041374794046\"\n" +
            "    },\n" +
            "    \"audio_muted\": false,\n" +
            "    \"poster_frame_index\": 87,\n" +
            "    \"clips_segments_metadata\": {\n" +
            "        \"num_segments\": 2,\n" +
            "        \"clips_segments\": [\n" +
            "            {\n" +
            "                \"index\": 0,\n" +
            "                \"face_effect_id\": null,\n" +
            "                \"speed\": 100,\n" +
            "                \"source_type\": \"0\",\n" +
            "                \"duration_ms\": 27025,\n" +
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
            "                \"duration_ms\": 511,\n" +
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
            "            \"artist_name\": \"oyeeditorranna\",\n" +
            "            \"audio_asset_id\": \"5367993556626807\",\n" +
            "            \"audio_cluster_id\": null,\n" +
            "            \"track_name\": \"Dekha Ek Khwaab x Laila ~ Anna x SushYohan Mashup\",\n" +
            "            \"is_picked_precapture\": \"1\",\n" +
            "            \"original_media_id\": \"2895955041374794046\"\n" +
            "        }\n" +
            "    }\n" +
            "}";
}
