
package in.semibit.media.common.igclientext.post.model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class User {

    @SerializedName("pk")
    @Expose
    private Long pk;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("is_private")
    @Expose
    private Boolean isPrivate;
    @SerializedName("profile_pic_url")
    @Expose
    private String profilePicUrl;
    @SerializedName("profile_pic_id")
    @Expose
    private String profilePicId;
    @SerializedName("friendship_status")
    @Expose
    private FriendshipStatus friendshipStatus;
    @SerializedName("is_verified")
    @Expose
    private Boolean isVerified;
    @SerializedName("has_anonymous_profile_picture")
    @Expose
    private Boolean hasAnonymousProfilePicture;
    @SerializedName("is_unpublished")
    @Expose
    private Boolean isUnpublished;
    @SerializedName("is_favorite")
    @Expose
    private Boolean isFavorite;
    @SerializedName("latest_reel_media")
    @Expose
    private Integer latestReelMedia;
    @SerializedName("has_highlight_reels")
    @Expose
    private Boolean hasHighlightReels;
    @SerializedName("transparency_product_enabled")
    @Expose
    private Boolean transparencyProductEnabled;
    @SerializedName("account_badges")
    @Expose
    private List<Object> accountBadges = null;
    @SerializedName("fan_club_info")
    @Expose
    private FanClubInfo fanClubInfo;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicId() {
        return profilePicId;
    }

    public void setProfilePicId(String profilePicId) {
        this.profilePicId = profilePicId;
    }

    public FriendshipStatus getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getHasAnonymousProfilePicture() {
        return hasAnonymousProfilePicture;
    }

    public void setHasAnonymousProfilePicture(Boolean hasAnonymousProfilePicture) {
        this.hasAnonymousProfilePicture = hasAnonymousProfilePicture;
    }

    public Boolean getIsUnpublished() {
        return isUnpublished;
    }

    public void setIsUnpublished(Boolean isUnpublished) {
        this.isUnpublished = isUnpublished;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Integer getLatestReelMedia() {
        return latestReelMedia;
    }

    public void setLatestReelMedia(Integer latestReelMedia) {
        this.latestReelMedia = latestReelMedia;
    }

    public Boolean getHasHighlightReels() {
        return hasHighlightReels;
    }

    public void setHasHighlightReels(Boolean hasHighlightReels) {
        this.hasHighlightReels = hasHighlightReels;
    }

    public Boolean getTransparencyProductEnabled() {
        return transparencyProductEnabled;
    }

    public void setTransparencyProductEnabled(Boolean transparencyProductEnabled) {
        this.transparencyProductEnabled = transparencyProductEnabled;
    }

    public List<Object> getAccountBadges() {
        return accountBadges;
    }

    public void setAccountBadges(List<Object> accountBadges) {
        this.accountBadges = accountBadges;
    }

    public FanClubInfo getFanClubInfo() {
        return fanClubInfo;
    }

    public void setFanClubInfo(FanClubInfo fanClubInfo) {
        this.fanClubInfo = fanClubInfo;
    }

}
