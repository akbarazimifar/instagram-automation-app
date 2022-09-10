package in.semibit.media.common.igclientext.likes;

import com.github.instagram4j.instagram4j.responses.IGResponse;

import java.util.ArrayList;
import java.util.List;

import in.semibit.media.common.igclientext.likes.model.LikeInfoModel;
import in.semibit.media.common.igclientext.post.model.User;

public class LikeInfoResponse extends IGResponse {

    LikeInfoModel post;

    public LikeInfoModel getPost() {
        return post;
    }

    public void setPost(LikeInfoModel post) {
        this.post = post;
    }

    public List<User> getLikers(){
        if(post!=null && post.getUsers()!=null && !post.getUsers().isEmpty())
        return post.getUsers();
        return new ArrayList<>();
    }

}
