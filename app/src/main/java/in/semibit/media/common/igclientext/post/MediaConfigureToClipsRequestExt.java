package in.semibit.media.common.igclientext.post;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.models.location.Location;
import com.github.instagram4j.instagram4j.models.media.UserTags;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.semibit.ezandroidutils.EzUtils;

import java.util.Collections;

import in.semibit.media.common.igclientext.post.model.PostItem;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MediaConfigureToClipsRequestExt extends IGPostRequest<MediaResponse.MediaConfigureToClipsResponse> {
    @NonNull
    private MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload payload;

    public MediaConfigureToClipsRequestExt(@NonNull MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload payload) {
        this.payload = payload;
    }

    @Override
    protected RequestBody getRequestBody(IGClient client) {
        if (getPayload(client) == null) {
            return RequestBody.create("", null);
        }
        String payload = IGUtils.objectToJson(getPayload(client) instanceof IGPayload
                ? client.setIGPayloadDefaults((IGPayload) getPayload(client))
                : getPayload(client));
        EzUtils.e("Payload", payload);
        if (this.payload.originalMediaId != null)
            payload = RemixPayload.getRemixPayload((MediaConfigureToClipsPayload) (client.setIGPayloadDefaults((IGPayload) getPayload(client))));
        if (isSigned()) {
            return RequestBody.create(IGUtils.generateSignature(payload),
                    MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"));
        } else {
            return RequestBody.create(payload, MediaType.parse("application/json; charset=UTF-8"));
        }
    }

    @Override
    protected IGPayload getPayload(IGClient client) {
        return payload;
    }

    @Override
    public String path() {
        return "media/configure_to_clips/?video=1";
    }

    @Override
    public Class<MediaResponse.MediaConfigureToClipsResponse> getResponseType() {
        return MediaResponse.MediaConfigureToClipsResponse.class;
    }

    @Override
    protected Request.Builder applyHeaders(IGClient client, Request.Builder req) {
        Request.Builder builder = super.applyHeaders(client, req);
        return builder;
    }

    @Data
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @Setter
    public static class MediaConfigureToClipsPayload extends IGPayload {
        private String upload_id;
        private String caption = "";
        private String source_type = "4";
        private String feed_show = "1";
        private String length;
        private String retryContext =
                "{\"num_step_auto_retry\":0,\"num_reupload\":0,\"num_step_manual_retry\":0}";
        private String disable_comments;
        private String location;
        private String usertags;

        public PostItem originalMediaId;

        public String upload_id() {
            return upload_id;
        }

        public String caption() {
            return caption;
        }

        public MediaConfigureToClipsPayload originalMediaId(PostItem originalMediaId) {
            this.originalMediaId = originalMediaId;
            return this;
        }

        public MediaConfigureToClipsPayload caption(String caption) {
            this.caption = caption;
            return this;
        }

        public MediaConfigureToClipsPayload upload_id(String upload_id) {
            this.upload_id = upload_id;
            return this;
        }

        public MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload location(Location loc) {
            Location payloadLoc = new Location();

            payloadLoc.setExternal_id(loc.getExternal_id());
            payloadLoc.setName(loc.getName());
            payloadLoc.setAddress(loc.getAddress());
            payloadLoc.setLat(loc.getLat());
            payloadLoc.setLng(loc.getLng());
            payloadLoc.setExternal_source(loc.getExternal_source());
            payloadLoc.put(payloadLoc.getExternal_source() + "_id", payloadLoc.getExternal_id());
            this.location = IGUtils.objectToJson(payloadLoc);
            this.put("geotag_enabled", "1");
            this.put("posting_latitude", payloadLoc.getLat().toString());
            this.put("posting_longitude", payloadLoc.getLng().toString());
            this.put("media_latitude", payloadLoc.getLat().toString());
            this.put("media_longitude", payloadLoc.getLng().toString());

            return this;
        }

        public MediaConfigureToClipsRequestExt.MediaConfigureToClipsPayload usertags(UserTags.UserTagPayload... tags) {
            this.usertags = IGUtils.objectToJson(Collections.singletonMap("in", tags));

            return this;
        }

        public String usertags() {
            return this.usertags;
        }
    }

}
