package in.semibit.media.common.igclientext.likes;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;

import in.semibit.media.common.igclientext.StringIGResponse;
import lombok.Data;
import lombok.NonNull;
import okhttp3.Response;

public class LikeUnlikePostRequest extends IGPostRequest<StringIGResponse> {

    @NonNull
    public String mediaId;
    public String action = "like";

    public LikeUnlikePostRequest(@NonNull String mediaId) {
        this.mediaId = mediaId;
    }

    public LikeUnlikePostRequest(@NonNull String mediaId, String action) {
        this.mediaId = mediaId;
        this.action = action;
    }

    @Override
    public StringIGResponse parseResponse(kotlin.Pair<Response, String> response) {
        StringIGResponse stringIGResponse = new StringIGResponse(response.getSecond());
        stringIGResponse.setStatusCode(response.getFirst().code());
        return stringIGResponse;
    }

    @Override
    protected IGPayload getPayload(IGClient client) {
        return new LikePostPayload(mediaId);
    }


    @Override
    public String path() {
        return "media/" + mediaId + "/"+action+"/";
    }

    @Override
    public Class<StringIGResponse> getResponseType() {
        return StringIGResponse.class;
    }

    @Data
    public class LikePostPayload extends IGPayload {
        public String delivery_class = "organic";
        public String media_id;
        public String tap_source = "button";
        public String nav_chain = "MainFeedFragment:feed_timeline:1:cold_start:10#230#301:2935540002369486928,UserDetailFragment:profile:2:media_owner::,ProfileMediaTabFragment:profile:3:button::,FollowListFragment:following:4:button::,FollowListFragment:followers:5:button::,UserDetailFragment:profile:10:button::,ContextualFeedFragment:feed_contextual_profile:15:button::";


        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }

        public LikePostPayload(String upload_id) {
            this.media_id = upload_id;
        }
    }

}
