package in.semibit.instadp.common.igclientext.post;

import com.github.instagram4j.instagram4j.responses.IGResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import in.semibit.instadp.common.igclientext.post.model.PostInfoModel;
import in.semibit.instadp.common.igclientext.post.model.PostItem;

public class PostInfoResponse extends IGResponse {

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
