package in.semibit.instadp.common.igclientext.likes;

import com.github.instagram4j.instagram4j.exceptions.IGResponseException;
import com.github.instagram4j.instagram4j.requests.IGGetRequest;
import com.google.gson.Gson;

import in.semibit.instadp.common.igclientext.post.model.PostInfoModel;
import okhttp3.Response;

public class LikeInfoRequest extends IGGetRequest<LikeInfoResponse> {

    String shortcode;
    Gson gson = new Gson();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    @Override
    public LikeInfoResponse parseResponse(kotlin.Pair<Response, String> response) {

        LikeInfoResponse igResponse = null;
        try {
            igResponse = parseResponse(response.getSecond());
            igResponse.setStatusCode(response.getFirst().code());
            if (!response.getFirst().isSuccessful() || (igResponse.getStatus() != null && igResponse.getStatus().equals("fail"))) {
                throw new IGResponseException(igResponse);
            }
            PostInfoModel post;

            post = gson.fromJson(response.getSecond(), PostInfoModel.class);
            post.setJsonResponse(response.getSecond());
            igResponse.setPost(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return igResponse;

    }

    public LikeInfoRequest(String shortcode) {
        this.shortcode = shortcode;
    }

    @Override
    public String path() {
        String url = "https://www.instagram.com/graphql/query/?query_hash=9f8827793ef34641b2fb195d4d41151c&variables={%22shortcode%22:%22" + shortcode + "%22,%22child_comment_count%22:3,%22fetch_comment_count%22:40,%22parent_comment_count%22:24,%22has_threaded_comments%22:true}";
        String mediaId = getIdFromCode(shortcode);
        url = "media/" + mediaId + "/info";
        return url;
    }

    @Override
    public Class<LikeInfoResponse> getResponseType() {
        return LikeInfoResponse.class;
    }


    public static String getIdFromCode(String code) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        long id = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            id = id * 64 + alphabet.indexOf(c);
        }
        return id + "";
    }

    public static String getCodeFromId(String id) {
        String[] parts = id.split("_");
        id = parts[0];
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder code = new StringBuilder();
        long longId = Long.parseLong(id);
        while (longId > 0) {
            long index = longId % 64;
            longId = (longId - index) / 64;
            code.insert(0, alphabet.charAt((int) index));
        }
        return code.toString();
    }

}
