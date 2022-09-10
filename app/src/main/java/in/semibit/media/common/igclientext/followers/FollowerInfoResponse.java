package in.semibit.media.common.igclientext.followers;

import com.github.instagram4j.instagram4j.responses.IGResponse;

import java.util.ArrayList;
import java.util.List;

import in.semibit.media.common.igclientext.followers.model.FollowerInfoModel;
import in.semibit.media.common.igclientext.post.model.User;

public class FollowerInfoResponse extends IGResponse {

    FollowerInfoModel followerInfoModel;

    public FollowerInfoModel getFollowerModel() {
        return followerInfoModel;
    }

    public void setFollowerInfoModel(FollowerInfoModel followerInfoModel) {
        this.followerInfoModel = followerInfoModel;
    }

    public List<User> getFollowers(){
        if(followerInfoModel !=null && followerInfoModel.getUsers()!=null && !followerInfoModel.getUsers().isEmpty())
        return followerInfoModel.getUsers();
        return new ArrayList<>();
    }

}
