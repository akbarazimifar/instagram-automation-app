package in.semibit.media.common.igclientext.followers;

import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.requests.IGGetRequest;
import com.google.gson.Gson;

import in.semibit.media.common.igclientext.followers.model.FollowerInfoModel;
import okhttp3.Response;

public class FollowerInfoRequest extends IGGetRequest<FollowerInfoResponse> {

    private final int count;
    String pk;
    String maxId;
    Gson gson = new Gson();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    public FollowerInfoResponse parseResponse(kotlin.Pair<Response, String> response) {

        FollowerInfoResponse igResponse = null;
        try {
            igResponse = parseResponse(response.getSecond());
            igResponse.setStatusCode(response.getFirst().code());
            if (!response.getFirst().isSuccessful() || (igResponse.getStatus() != null && igResponse.getStatus().equals("fail"))) {
                throw new IGResponseException(igResponse);
            }
            FollowerInfoModel post;

            post = gson.fromJson(response.getSecond(), FollowerInfoModel.class);
            post.setJsonResponse(response.getSecond());
            igResponse.setFollowerInfoModel(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return igResponse;

    }

    public FollowerInfoRequest(String pk,int count,String maxId) {
        this.pk = pk;
        this.count = count;
        this.maxId = maxId;
    }

    @Override
    public String path() {
        String url = "friendships/" + pk + "/followers?count="+count+"&search_surface=follow_list_page";
        if(maxId != null && maxId.length() > 1){
            url = url + "&max_id="+maxId;
        }
        return url;
    }

    @Override
    public Class<FollowerInfoResponse> getResponseType() {
        return FollowerInfoResponse.class;
    }

}
