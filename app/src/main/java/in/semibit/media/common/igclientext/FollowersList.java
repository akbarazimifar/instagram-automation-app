package in.semibit.media.common.igclientext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.semibit.media.common.database.IdentifiedModel;

public class FollowersList implements IdentifiedModel {
    String id = "to_be_follow"; //ig_followers,ig_following,to_be_unfollow,to_be_follow
    Set<String> followIds = new HashSet<>();


    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getFollowIds() {
        return followIds;
    }

    public void setFollowIds(Set<String> followIds) {
        this.followIds = followIds;
    }
}
