package in.semibit.media.common.igclientext.followers.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

import in.semibit.media.common.igclientext.JsonInfoModel;
import in.semibit.media.common.igclientext.post.model.User;

@Generated("jsonschema2pojo")
public class FollowerInfoModel extends JsonInfoModel {

    @SerializedName("users")
    @Expose
    private List<User> users = null;
    @SerializedName("big_list")
    @Expose
    private Boolean bigList;
    @SerializedName("page_size")
    @Expose
    private Integer pageSize;
    @SerializedName("next_max_id")
    @Expose
    private String nextMaxId;
    @SerializedName("more_groups_available")
    @Expose
    private Boolean moreGroupsAvailable;
    @SerializedName("has_more")
    @Expose
    private Boolean hasMore;
    @SerializedName("status")
    @Expose
    private String status;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Boolean getBigList() {
        return bigList;
    }

    public void setBigList(Boolean bigList) {
        this.bigList = bigList;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getNextMaxId() {
        return nextMaxId;
    }

    public void setNextMaxId(String nextMaxId) {
        this.nextMaxId = nextMaxId;
    }

    public Boolean getMoreGroupsAvailable() {
        return moreGroupsAvailable;
    }

    public void setMoreGroupsAvailable(Boolean moreGroupsAvailable) {
        this.moreGroupsAvailable = moreGroupsAvailable;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
