package in.semibit.media.common.igclientext.followers;

public class FollowingInfoRequest extends FollowerInfoRequest {

    public FollowingInfoRequest(String pk, int count, String maxId) {
        super(pk, count, maxId);
    }

    @Override
    public String path() {
        String url = "friendships/" + pk + "/following?count="+count+"&search_surface=follow_list_page";
        if(maxId != null && maxId.length() > 1){
            url = url + "&max_id="+maxId;
        }
        return url;
    }

}
