package in.semibit.instadp.common.igclientext.likes;

import com.github.instagram4j.instagram4j.responses.IGResponse;

import in.semibit.instadp.common.igclientext.post.model.PostInfoModel;
import in.semibit.instadp.common.igclientext.post.model.PostItem;

public class LikeInfoResponse extends IGResponse {

    PostInfoModel post;

    public PostInfoModel getPost() {
        return post;
    }

    public void setPost(PostInfoModel post) {
        this.post = post;
    }

    public PostItem getFirstPost(){
        if(post!=null && post.getItems()!=null && !post.getItems().isEmpty())
        return post.getItems().stream().findFirst().orElse(null);
        return null;
    }

}
