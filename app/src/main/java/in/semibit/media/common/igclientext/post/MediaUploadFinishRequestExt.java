package in.semibit.media.common.igclientext.post;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;

import in.semibit.media.common.igclientext.StringIGResponse;
import lombok.Data;
import lombok.NonNull;
import okhttp3.Response;

public class MediaUploadFinishRequestExt extends IGPostRequest<StringIGResponse> {

    @NonNull
    public String uploadId;

    public MediaUploadFinishRequestExt(@NonNull String uploadId) {
        this.uploadId = uploadId;
    }

    @Override
    public StringIGResponse parseResponse(kotlin.Pair<Response, String> response) {
        StringIGResponse stringIGResponse = new StringIGResponse(response.getSecond());
        stringIGResponse.setStatusCode(response.getFirst().code());
        return stringIGResponse;
    }

    @Override
    protected IGPayload getPayload(IGClient client) {
        return new MediaUploadFinishPayload(uploadId);
    }


    @Override
    public String path() {
        return "media/upload_finish/";
    }

    @Override
    public Class<StringIGResponse> getResponseType() {
        return StringIGResponse.class;
    }

    @Data
    public class MediaUploadFinishPayload extends IGPayload {
        public String upload_id = uploadId;

        public String getUpload_id() {
            return upload_id;
        }

        public void setUpload_id(String upload_id) {
            this.upload_id = upload_id;
        }

        public MediaUploadFinishPayload(String upload_id) {
            this.upload_id = upload_id;
        }
    }

}
