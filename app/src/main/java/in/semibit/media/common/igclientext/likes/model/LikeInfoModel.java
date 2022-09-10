package in.semibit.media.common.igclientext.likes.model;


import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import in.semibit.media.common.igclientext.JsonInfoModel;
import in.semibit.media.common.igclientext.post.model.User;

@Generated("jsonschema2pojo")
public class LikeInfoModel extends JsonInfoModel {

    @SerializedName("users")
    @Expose
    private List<User> users = null;
    @SerializedName("user_count")
    @Expose
    private Integer userCount;
    @SerializedName("status")
    @Expose
    private String status;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
