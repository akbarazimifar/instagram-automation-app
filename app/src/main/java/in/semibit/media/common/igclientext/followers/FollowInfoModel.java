package in.semibit.media.common.igclientext.followers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FollowInfoModel {

    @SerializedName("blocking")
    @Expose
    private Boolean blocking;
    @SerializedName("followed_by")
    @Expose
    private Boolean followedBy;
    @SerializedName("following")
    @Expose
    private Boolean following;
    @SerializedName("incoming_request")
    @Expose
    private Boolean incomingRequest;
    @SerializedName("is_bestie")
    @Expose
    private Boolean isBestie;
    @SerializedName("is_blocking_reel")
    @Expose
    private Boolean isBlockingReel;
    @SerializedName("is_muting_reel")
    @Expose
    private Boolean isMutingReel;
    @SerializedName("is_private")
    @Expose
    private Boolean isPrivate;
    @SerializedName("is_restricted")
    @Expose
    private Boolean isRestricted;
    @SerializedName("muting")
    @Expose
    private Boolean muting;
    @SerializedName("outgoing_request")
    @Expose
    private Boolean outgoingRequest;
    @SerializedName("is_feed_favorite")
    @Expose
    private Boolean isFeedFavorite;
    @SerializedName("subscribed")
    @Expose
    private Boolean subscribed;
    @SerializedName("is_eligible_to_subscribe")
    @Expose
    private Boolean isEligibleToSubscribe;
    @SerializedName("is_supervised_by_viewer")
    @Expose
    private Boolean isSupervisedByViewer;
    @SerializedName("is_guardian_of_viewer")
    @Expose
    private Boolean isGuardianOfViewer;
    @SerializedName("status")
    @Expose
    private String status;

    public Boolean getBlocking() {
        return blocking;
    }

    public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }

    public Boolean getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(Boolean followedBy) {
        this.followedBy = followedBy;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public Boolean getIncomingRequest() {
        return incomingRequest;
    }

    public void setIncomingRequest(Boolean incomingRequest) {
        this.incomingRequest = incomingRequest;
    }

    public Boolean getIsBestie() {
        return isBestie;
    }

    public void setIsBestie(Boolean isBestie) {
        this.isBestie = isBestie;
    }

    public Boolean getIsBlockingReel() {
        return isBlockingReel;
    }

    public void setIsBlockingReel(Boolean isBlockingReel) {
        this.isBlockingReel = isBlockingReel;
    }

    public Boolean getIsMutingReel() {
        return isMutingReel;
    }

    public void setIsMutingReel(Boolean isMutingReel) {
        this.isMutingReel = isMutingReel;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(Boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public Boolean getMuting() {
        return muting;
    }

    public void setMuting(Boolean muting) {
        this.muting = muting;
    }

    public Boolean getOutgoingRequest() {
        return outgoingRequest;
    }

    public void setOutgoingRequest(Boolean outgoingRequest) {
        this.outgoingRequest = outgoingRequest;
    }

    public Boolean getIsFeedFavorite() {
        return isFeedFavorite;
    }

    public void setIsFeedFavorite(Boolean isFeedFavorite) {
        this.isFeedFavorite = isFeedFavorite;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    public Boolean getIsEligibleToSubscribe() {
        return isEligibleToSubscribe;
    }

    public void setIsEligibleToSubscribe(Boolean isEligibleToSubscribe) {
        this.isEligibleToSubscribe = isEligibleToSubscribe;
    }

    public Boolean getIsSupervisedByViewer() {
        return isSupervisedByViewer;
    }

    public void setIsSupervisedByViewer(Boolean isSupervisedByViewer) {
        this.isSupervisedByViewer = isSupervisedByViewer;
    }

    public Boolean getIsGuardianOfViewer() {
        return isGuardianOfViewer;
    }

    public void setIsGuardianOfViewer(Boolean isGuardianOfViewer) {
        this.isGuardianOfViewer = isGuardianOfViewer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
