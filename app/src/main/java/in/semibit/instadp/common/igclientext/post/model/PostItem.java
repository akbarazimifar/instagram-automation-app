
package in.semibit.instadp.common.igclientext.post.model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class PostItem {

    @SerializedName("taken_at")
    @Expose
    private Integer takenAt;
    @SerializedName("pk")
    @Expose
    private Long pk;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("device_timestamp")
    @Expose
    private Long deviceTimestamp;
    @SerializedName("media_type")
    @Expose
    private Integer mediaType;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("client_cache_key")
    @Expose
    private String clientCacheKey;
    @SerializedName("filter_type")
    @Expose
    private Integer filterType;
    @SerializedName("is_unified_video")
    @Expose
    private Boolean isUnifiedVideo;
    @SerializedName("should_request_ads")
    @Expose
    private Boolean shouldRequestAds;
    @SerializedName("caption_is_edited")
    @Expose
    private Boolean captionIsEdited;
    @SerializedName("like_and_view_counts_disabled")
    @Expose
    private Boolean likeAndViewCountsDisabled;
    @SerializedName("commerciality_status")
    @Expose
    private String commercialityStatus;
    @SerializedName("is_paid_partnership")
    @Expose
    private Boolean isPaidPartnership;
    @SerializedName("is_visual_reply_commenter_notice_enabled")
    @Expose
    private Boolean isVisualReplyCommenterNoticeEnabled;
    @SerializedName("original_media_has_visual_reply_media")
    @Expose
    private Boolean originalMediaHasVisualReplyMedia;
    @SerializedName("has_delayed_metadata")
    @Expose
    private Boolean hasDelayedMetadata;
    @SerializedName("comment_likes_enabled")
    @Expose
    private Boolean commentLikesEnabled;
    @SerializedName("comment_threading_enabled")
    @Expose
    private Boolean commentThreadingEnabled;
    @SerializedName("has_more_comments")
    @Expose
    private Boolean hasMoreComments;
    @SerializedName("max_num_visible_preview_comments")
    @Expose
    private Integer maxNumVisiblePreviewComments;
    @SerializedName("preview_comments")
    @Expose
    private List<Object> previewComments = null;
    @SerializedName("can_view_more_preview_comments")
    @Expose
    private Boolean canViewMorePreviewComments;
    @SerializedName("comment_count")
    @Expose
    private Integer commentCount;
    @SerializedName("hide_view_all_comment_entrypoint")
    @Expose
    private Boolean hideViewAllCommentEntrypoint;
    @SerializedName("inline_composer_display_condition")
    @Expose
    private String inlineComposerDisplayCondition;
    @SerializedName("inline_composer_imp_trigger_time")
    @Expose
    private Integer inlineComposerImpTriggerTime;
    @SerializedName("image_versions2")
    @Expose
    private ImageVersions2 imageVersions2;
    @SerializedName("original_width")
    @Expose
    private Integer originalWidth;
    @SerializedName("original_height")
    @Expose
    private Integer originalHeight;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("can_viewer_reshare")
    @Expose
    private Boolean canViewerReshare;
    @SerializedName("like_count")
    @Expose
    private Integer likeCount;
    @SerializedName("has_liked")
    @Expose
    private Boolean hasLiked;
    @SerializedName("top_likers")
    @Expose
    private List<String> topLikers = null;
    @SerializedName("facepile_top_likers")
    @Expose
    private List<FacepileTopLiker> facepileTopLikers = null;
    @SerializedName("photo_of_you")
    @Expose
    private Boolean photoOfYou;
    @SerializedName("is_organic_product_tagging_eligible")
    @Expose
    private Boolean isOrganicProductTaggingEligible;
    @SerializedName("can_see_insights_as_brand")
    @Expose
    private Boolean canSeeInsightsAsBrand;
    @SerializedName("caption")
    @Expose
    private Caption caption;
    @SerializedName("featured_products_cta")
    @Expose
    private Object featuredProductsCta;
    @SerializedName("comment_inform_treatment")
    @Expose
    private CommentInformTreatment commentInformTreatment;
    @SerializedName("sharing_friction_info")
    @Expose
    private SharingFrictionInfo sharingFrictionInfo;
    @SerializedName("can_viewer_save")
    @Expose
    private Boolean canViewerSave;
    @SerializedName("is_in_profile_grid")
    @Expose
    private Boolean isInProfileGrid;
    @SerializedName("profile_grid_control_enabled")
    @Expose
    private Boolean profileGridControlEnabled;
    @SerializedName("organic_tracking_token")
    @Expose
    private String organicTrackingToken;
    @SerializedName("has_shared_to_fb")
    @Expose
    private Integer hasSharedToFb;
    @SerializedName("product_type")
    @Expose
    private String productType;
    @SerializedName("deleted_reason")
    @Expose
    private Integer deletedReason;
    @SerializedName("integrity_review_decision")
    @Expose
    private String integrityReviewDecision;
    @SerializedName("commerce_integrity_review_decision")
    @Expose
    private Object commerceIntegrityReviewDecision;
    @SerializedName("music_metadata")
    @Expose
    private MusicMetadata musicMetadata;
    @SerializedName("is_artist_pick")
    @Expose
    private Boolean isArtistPick;

    public Integer getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(Integer takenAt) {
        this.takenAt = takenAt;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDeviceTimestamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimestamp(Long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }

    public Integer getMediaType() {
        return mediaType;
    }

    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientCacheKey() {
        return clientCacheKey;
    }

    public void setClientCacheKey(String clientCacheKey) {
        this.clientCacheKey = clientCacheKey;
    }

    public Integer getFilterType() {
        return filterType;
    }

    public void setFilterType(Integer filterType) {
        this.filterType = filterType;
    }

    public Boolean getIsUnifiedVideo() {
        return isUnifiedVideo;
    }

    public void setIsUnifiedVideo(Boolean isUnifiedVideo) {
        this.isUnifiedVideo = isUnifiedVideo;
    }

    public Boolean getShouldRequestAds() {
        return shouldRequestAds;
    }

    public void setShouldRequestAds(Boolean shouldRequestAds) {
        this.shouldRequestAds = shouldRequestAds;
    }

    public Boolean getCaptionIsEdited() {
        return captionIsEdited;
    }

    public void setCaptionIsEdited(Boolean captionIsEdited) {
        this.captionIsEdited = captionIsEdited;
    }

    public Boolean getLikeAndViewCountsDisabled() {
        return likeAndViewCountsDisabled;
    }

    public void setLikeAndViewCountsDisabled(Boolean likeAndViewCountsDisabled) {
        this.likeAndViewCountsDisabled = likeAndViewCountsDisabled;
    }

    public String getCommercialityStatus() {
        return commercialityStatus;
    }

    public void setCommercialityStatus(String commercialityStatus) {
        this.commercialityStatus = commercialityStatus;
    }

    public Boolean getIsPaidPartnership() {
        return isPaidPartnership;
    }

    public void setIsPaidPartnership(Boolean isPaidPartnership) {
        this.isPaidPartnership = isPaidPartnership;
    }

    public Boolean getIsVisualReplyCommenterNoticeEnabled() {
        return isVisualReplyCommenterNoticeEnabled;
    }

    public void setIsVisualReplyCommenterNoticeEnabled(Boolean isVisualReplyCommenterNoticeEnabled) {
        this.isVisualReplyCommenterNoticeEnabled = isVisualReplyCommenterNoticeEnabled;
    }

    public Boolean getOriginalMediaHasVisualReplyMedia() {
        return originalMediaHasVisualReplyMedia;
    }

    public void setOriginalMediaHasVisualReplyMedia(Boolean originalMediaHasVisualReplyMedia) {
        this.originalMediaHasVisualReplyMedia = originalMediaHasVisualReplyMedia;
    }

    public Boolean getHasDelayedMetadata() {
        return hasDelayedMetadata;
    }

    public void setHasDelayedMetadata(Boolean hasDelayedMetadata) {
        this.hasDelayedMetadata = hasDelayedMetadata;
    }

    public Boolean getCommentLikesEnabled() {
        return commentLikesEnabled;
    }

    public void setCommentLikesEnabled(Boolean commentLikesEnabled) {
        this.commentLikesEnabled = commentLikesEnabled;
    }

    public Boolean getCommentThreadingEnabled() {
        return commentThreadingEnabled;
    }

    public void setCommentThreadingEnabled(Boolean commentThreadingEnabled) {
        this.commentThreadingEnabled = commentThreadingEnabled;
    }

    public Boolean getHasMoreComments() {
        return hasMoreComments;
    }

    public void setHasMoreComments(Boolean hasMoreComments) {
        this.hasMoreComments = hasMoreComments;
    }

    public Integer getMaxNumVisiblePreviewComments() {
        return maxNumVisiblePreviewComments;
    }

    public void setMaxNumVisiblePreviewComments(Integer maxNumVisiblePreviewComments) {
        this.maxNumVisiblePreviewComments = maxNumVisiblePreviewComments;
    }

    public List<Object> getPreviewComments() {
        return previewComments;
    }

    public void setPreviewComments(List<Object> previewComments) {
        this.previewComments = previewComments;
    }

    public Boolean getCanViewMorePreviewComments() {
        return canViewMorePreviewComments;
    }

    public void setCanViewMorePreviewComments(Boolean canViewMorePreviewComments) {
        this.canViewMorePreviewComments = canViewMorePreviewComments;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Boolean getHideViewAllCommentEntrypoint() {
        return hideViewAllCommentEntrypoint;
    }

    public void setHideViewAllCommentEntrypoint(Boolean hideViewAllCommentEntrypoint) {
        this.hideViewAllCommentEntrypoint = hideViewAllCommentEntrypoint;
    }

    public String getInlineComposerDisplayCondition() {
        return inlineComposerDisplayCondition;
    }

    public void setInlineComposerDisplayCondition(String inlineComposerDisplayCondition) {
        this.inlineComposerDisplayCondition = inlineComposerDisplayCondition;
    }

    public Integer getInlineComposerImpTriggerTime() {
        return inlineComposerImpTriggerTime;
    }

    public void setInlineComposerImpTriggerTime(Integer inlineComposerImpTriggerTime) {
        this.inlineComposerImpTriggerTime = inlineComposerImpTriggerTime;
    }

    public ImageVersions2 getImageVersions2() {
        return imageVersions2;
    }

    public void setImageVersions2(ImageVersions2 imageVersions2) {
        this.imageVersions2 = imageVersions2;
    }

    public Integer getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(Integer originalWidth) {
        this.originalWidth = originalWidth;
    }

    public Integer getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(Integer originalHeight) {
        this.originalHeight = originalHeight;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getCanViewerReshare() {
        return canViewerReshare;
    }

    public void setCanViewerReshare(Boolean canViewerReshare) {
        this.canViewerReshare = canViewerReshare;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Boolean getHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(Boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public List<String> getTopLikers() {
        return topLikers;
    }

    public void setTopLikers(List<String> topLikers) {
        this.topLikers = topLikers;
    }

    public List<FacepileTopLiker> getFacepileTopLikers() {
        return facepileTopLikers;
    }

    public void setFacepileTopLikers(List<FacepileTopLiker> facepileTopLikers) {
        this.facepileTopLikers = facepileTopLikers;
    }

    public Boolean getPhotoOfYou() {
        return photoOfYou;
    }

    public void setPhotoOfYou(Boolean photoOfYou) {
        this.photoOfYou = photoOfYou;
    }

    public Boolean getIsOrganicProductTaggingEligible() {
        return isOrganicProductTaggingEligible;
    }

    public void setIsOrganicProductTaggingEligible(Boolean isOrganicProductTaggingEligible) {
        this.isOrganicProductTaggingEligible = isOrganicProductTaggingEligible;
    }

    public Boolean getCanSeeInsightsAsBrand() {
        return canSeeInsightsAsBrand;
    }

    public void setCanSeeInsightsAsBrand(Boolean canSeeInsightsAsBrand) {
        this.canSeeInsightsAsBrand = canSeeInsightsAsBrand;
    }

    public Caption getCaption() {
        return caption;
    }

    public void setCaption(Caption caption) {
        this.caption = caption;
    }

    public Object getFeaturedProductsCta() {
        return featuredProductsCta;
    }

    public void setFeaturedProductsCta(Object featuredProductsCta) {
        this.featuredProductsCta = featuredProductsCta;
    }

    public CommentInformTreatment getCommentInformTreatment() {
        return commentInformTreatment;
    }

    public void setCommentInformTreatment(CommentInformTreatment commentInformTreatment) {
        this.commentInformTreatment = commentInformTreatment;
    }

    public SharingFrictionInfo getSharingFrictionInfo() {
        return sharingFrictionInfo;
    }

    public void setSharingFrictionInfo(SharingFrictionInfo sharingFrictionInfo) {
        this.sharingFrictionInfo = sharingFrictionInfo;
    }

    public Boolean getCanViewerSave() {
        return canViewerSave;
    }

    public void setCanViewerSave(Boolean canViewerSave) {
        this.canViewerSave = canViewerSave;
    }

    public Boolean getIsInProfileGrid() {
        return isInProfileGrid;
    }

    public void setIsInProfileGrid(Boolean isInProfileGrid) {
        this.isInProfileGrid = isInProfileGrid;
    }

    public Boolean getProfileGridControlEnabled() {
        return profileGridControlEnabled;
    }

    public void setProfileGridControlEnabled(Boolean profileGridControlEnabled) {
        this.profileGridControlEnabled = profileGridControlEnabled;
    }

    public String getOrganicTrackingToken() {
        return organicTrackingToken;
    }

    public void setOrganicTrackingToken(String organicTrackingToken) {
        this.organicTrackingToken = organicTrackingToken;
    }

    public Integer getHasSharedToFb() {
        return hasSharedToFb;
    }

    public void setHasSharedToFb(Integer hasSharedToFb) {
        this.hasSharedToFb = hasSharedToFb;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getDeletedReason() {
        return deletedReason;
    }

    public void setDeletedReason(Integer deletedReason) {
        this.deletedReason = deletedReason;
    }

    public String getIntegrityReviewDecision() {
        return integrityReviewDecision;
    }

    public void setIntegrityReviewDecision(String integrityReviewDecision) {
        this.integrityReviewDecision = integrityReviewDecision;
    }

    public Object getCommerceIntegrityReviewDecision() {
        return commerceIntegrityReviewDecision;
    }

    public void setCommerceIntegrityReviewDecision(Object commerceIntegrityReviewDecision) {
        this.commerceIntegrityReviewDecision = commerceIntegrityReviewDecision;
    }

    public MusicMetadata getMusicMetadata() {
        return musicMetadata;
    }

    public void setMusicMetadata(MusicMetadata musicMetadata) {
        this.musicMetadata = musicMetadata;
    }

    public Boolean getIsArtistPick() {
        return isArtistPick;
    }

    public void setIsArtistPick(Boolean isArtistPick) {
        this.isArtistPick = isArtistPick;
    }




    ///VIDEO


    @SerializedName("video_dash_manifest")
    @Expose
    private String videoDashManifest;
    @SerializedName("video_codec")
    @Expose
    private String videoCodec;
    @SerializedName("number_of_qualities")
    @Expose
    private Integer numberOfQualities;
    @SerializedName("video_versions")
    @Expose
    private List<VideoVersion> videoVersions = null;
    @SerializedName("has_audio")
    @Expose
    private Boolean hasAudio;
    @SerializedName("video_duration")
    @Expose
    private Double videoDuration;
    @SerializedName("view_count")
    @Expose
    private Integer viewCount;
    @SerializedName("play_count")
    @Expose
    private Integer playCount;

    public String getVideoDashManifest() {
        return videoDashManifest;
    }

    public void setVideoDashManifest(String videoDashManifest) {
        this.videoDashManifest = videoDashManifest;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public Integer getNumberOfQualities() {
        return numberOfQualities;
    }

    public void setNumberOfQualities(Integer numberOfQualities) {
        this.numberOfQualities = numberOfQualities;
    }

    public List<VideoVersion> getVideoVersions() {
        return videoVersions;
    }

    public void setVideoVersions(List<VideoVersion> videoVersions) {
        this.videoVersions = videoVersions;
    }

    public Boolean getHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(Boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public Double getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Double videoDuration) {
        this.videoDuration = videoDuration;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

}
