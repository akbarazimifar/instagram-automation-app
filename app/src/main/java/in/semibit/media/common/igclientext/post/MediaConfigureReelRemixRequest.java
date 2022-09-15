package in.semibit.media.common.igclientext.post;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.models.location.Location;
import com.github.instagram4j.instagram4j.models.media.UserTags;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;
import com.github.instagram4j.instagram4j.responses.media.MediaResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;

import java.util.Collections;

import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MediaConfigureReelRemixRequest extends IGPostRequest<MediaResponse.MediaConfigureTimelineResponse> {
    @NonNull
    private MediaConfigureReelRemixRequest.MediaConfigurePayload payload;

    @Override
    protected RequestBody getRequestBody(IGClient client) {
        if (getPayload(client) == null) {
            return RequestBody.create("", null);
        }
        String payloadStr = IGUtils.objectToJson(getPayload(client) instanceof IGPayload
                ? client.setIGPayloadDefaults((IGPayload) getPayload(client))
                : getPayload(client));
        payloadStr = RemixPayload.getRemixPayload(
                (MediaConfigureReelRemixRequest.MediaConfigurePayload) client.setIGPayloadDefaults(payload));
        if (isSigned()) {
            return RequestBody.create(IGUtils.generateSignature(payloadStr),
                    MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"));
        } else {
            return RequestBody.create(payloadStr, MediaType.parse("application/json; charset=UTF-8"));
        }
    }

    public MediaConfigureReelRemixRequest(@NonNull MediaConfigureReelRemixRequest.MediaConfigurePayload payload) {
        this.payload = payload;
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
    public Class<MediaResponse.MediaConfigureTimelineResponse> getResponseType() {
        return MediaResponse.MediaConfigureTimelineResponse.class;
    }

    @Data
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Setter
    public static class MediaConfigurePayload extends IGPayload {
        private String upload_id;
        private String caption = "";
        private String disable_comments;
        private String location;
        private String usertags;

        public String  caption() {
            return caption;
        }

        public String  upload_id() {
            return upload_id;
        }

        public MediaConfigurePayload upload_id(String upload_id) {
            this.upload_id = upload_id;
            return this;
        }

        public MediaConfigurePayload caption(String caption) {
            this.caption = caption;
            return this;
        }


        public MediaConfigureReelRemixRequest.MediaConfigurePayload location(Location loc) {
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

        public MediaConfigureReelRemixRequest.MediaConfigurePayload usertags(UserTags.UserTagPayload... tags) {
            this.usertags = IGUtils.objectToJson(Collections.singletonMap("in", tags));

            return this;
        }

        public String usertags() {
            return this.usertags;
        }

    }

}
