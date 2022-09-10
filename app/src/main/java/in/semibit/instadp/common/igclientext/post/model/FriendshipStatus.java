
package in.semibit.instadp.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class FriendshipStatus {

    @SerializedName("following")
    @Expose
    private Boolean following;
    @SerializedName("outgoing_request")
    @Expose
    private Boolean outgoingRequest;
    @SerializedName("is_bestie")
    @Expose
    private Boolean isBestie;
    @SerializedName("is_restricted")
    @Expose
    private Boolean isRestricted;
    @SerializedName("is_feed_favorite")
    @Expose
    private Boolean isFeedFavorite;

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Boolean getOutgoingRequest() {
        return outgoingRequest;
    }

    public void setOutgoingRequest(Boolean outgoingRequest) {
        this.outgoingRequest = outgoingRequest;
    }

    public Boolean getIsBestie() {
        return isBestie;
    }

    public void setIsBestie(Boolean isBestie) {
        this.isBestie = isBestie;
    }

    public Boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(Boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public Boolean getIsFeedFavorite() {
        return isFeedFavorite;
    }

    public void setIsFeedFavorite(Boolean isFeedFavorite) {
        this.isFeedFavorite = isFeedFavorite;
    }

}
