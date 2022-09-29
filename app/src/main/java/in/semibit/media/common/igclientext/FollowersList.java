package in.semibit.media.common.igclientext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.semibit.media.common.database.IdentifiedModel;

public class FollowersList implements IdentifiedModel {
    String id = "to_be_follow"; //ig_followers,ig_following,to_be_unfollow,to_be_follow
    String type = "to_be_follow";
    List<String> followIds = new ArrayList<>();


    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFollowIds() {
        return followIds;
    }

    public void setFollowIds(List<String> followIds) {
        this.followIds = followIds;
    }

    public String readJoinedFollowerIds(){
        return String.join(",",""+getFollowIds());
    }
}
