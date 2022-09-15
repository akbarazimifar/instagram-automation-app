package in.semibit.media.common.igclientext.post.model;


import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ClipsMetadata {

    @SerializedName("music_info")
    @Expose
    private Object musicInfo;
    @SerializedName("original_sound_info")
    @Expose
    private OriginalSoundInfo originalSoundInfo;
    @SerializedName("audio_type")
    @Expose
    private String audioType;
    @SerializedName("music_canonical_id")
    @Expose
    private String musicCanonicalId;
    @SerializedName("featured_label")
    @Expose
    private Object featuredLabel;
    @SerializedName("mashup_info")
    @Expose
    private MashupInfo mashupInfo;
    @SerializedName("nux_info")
    @Expose
    private Object nuxInfo;
    @SerializedName("viewer_interaction_settings")
    @Expose
    private Object viewerInteractionSettings;

    @SerializedName("shopping_info")
    @Expose
    private Object shoppingInfo;

    @SerializedName("is_shared_to_fb")
    @Expose
    private Boolean isSharedToFb;
    @SerializedName("breaking_content_info")
    @Expose
    private Object breakingContentInfo;
    @SerializedName("challenge_info")
    @Expose
    private Object challengeInfo;
    @SerializedName("reels_on_the_rise_info")
    @Expose
    private Object reelsOnTheRiseInfo;
    @SerializedName("breaking_creator_info")
    @Expose
    private Object breakingCreatorInfo;
    @SerializedName("asset_recommendation_info")
    @Expose
    private Object assetRecommendationInfo;
    @SerializedName("contextual_highlight_info")
    @Expose
    private Object contextualHighlightInfo;
    @SerializedName("clips_creation_entry_point")
    @Expose
    private String clipsCreationEntryPoint;

    @SerializedName("template_info")
    @Expose
    private Object templateInfo;
    @SerializedName("is_fan_club_promo_video")
    @Expose
    private Object isFanClubPromoVideo;
    @SerializedName("disable_use_in_clips_client_cache")
    @Expose
    private Boolean disableUseInClipsClientCache;
    @SerializedName("content_appreciation_info")
    @Expose
    private Object contentAppreciationInfo;

    public Object getMusicInfo() {
        return musicInfo;
    }

    public void setMusicInfo(Object musicInfo) {
        this.musicInfo = musicInfo;
    }

    public OriginalSoundInfo getOriginalSoundInfo() {
        return originalSoundInfo;
    }

    public void setOriginalSoundInfo(OriginalSoundInfo originalSoundInfo) {
        this.originalSoundInfo = originalSoundInfo;
    }

    public String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public String getMusicCanonicalId() {
        return musicCanonicalId;
    }

    public void setMusicCanonicalId(String musicCanonicalId) {
        this.musicCanonicalId = musicCanonicalId;
    }

    public Object getFeaturedLabel() {
        return featuredLabel;
    }

    public void setFeaturedLabel(Object featuredLabel) {
        this.featuredLabel = featuredLabel;
    }

    public MashupInfo getMashupInfo() {
        return mashupInfo;
    }

    public void setMashupInfo(MashupInfo mashupInfo) {
        this.mashupInfo = mashupInfo;
    }

    public Object getNuxInfo() {
        return nuxInfo;
    }

    public void setNuxInfo(Object nuxInfo) {
        this.nuxInfo = nuxInfo;
    }

    public Object getViewerInteractionSettings() {
        return viewerInteractionSettings;
    }

    public void setViewerInteractionSettings(Object viewerInteractionSettings) {
        this.viewerInteractionSettings = viewerInteractionSettings;
    }

    public Object getShoppingInfo() {
        return shoppingInfo;
    }

    public void setShoppingInfo(Object shoppingInfo) {
        this.shoppingInfo = shoppingInfo;
    }

    public Boolean getIsSharedToFb() {
        return isSharedToFb;
    }

    public void setIsSharedToFb(Boolean isSharedToFb) {
        this.isSharedToFb = isSharedToFb;
    }

    public Object getBreakingContentInfo() {
        return breakingContentInfo;
    }

    public void setBreakingContentInfo(Object breakingContentInfo) {
        this.breakingContentInfo = breakingContentInfo;
    }

    public Object getChallengeInfo() {
        return challengeInfo;
    }

    public void setChallengeInfo(Object challengeInfo) {
        this.challengeInfo = challengeInfo;
    }

    public Object getReelsOnTheRiseInfo() {
        return reelsOnTheRiseInfo;
    }

    public void setReelsOnTheRiseInfo(Object reelsOnTheRiseInfo) {
        this.reelsOnTheRiseInfo = reelsOnTheRiseInfo;
    }

    public Object getBreakingCreatorInfo() {
        return breakingCreatorInfo;
    }

    public void setBreakingCreatorInfo(Object breakingCreatorInfo) {
        this.breakingCreatorInfo = breakingCreatorInfo;
    }

    public Object getAssetRecommendationInfo() {
        return assetRecommendationInfo;
    }

    public void setAssetRecommendationInfo(Object assetRecommendationInfo) {
        this.assetRecommendationInfo = assetRecommendationInfo;
    }

    public Object getContextualHighlightInfo() {
        return contextualHighlightInfo;
    }

    public void setContextualHighlightInfo(Object contextualHighlightInfo) {
        this.contextualHighlightInfo = contextualHighlightInfo;
    }

    public String getClipsCreationEntryPoint() {
        return clipsCreationEntryPoint;
    }

    public void setClipsCreationEntryPoint(String clipsCreationEntryPoint) {
        this.clipsCreationEntryPoint = clipsCreationEntryPoint;
    }


    public Object getTemplateInfo() {
        return templateInfo;
    }

    public void setTemplateInfo(Object templateInfo) {
        this.templateInfo = templateInfo;
    }

    public Object getIsFanClubPromoVideo() {
        return isFanClubPromoVideo;
    }

    public void setIsFanClubPromoVideo(Object isFanClubPromoVideo) {
        this.isFanClubPromoVideo = isFanClubPromoVideo;
    }

    public Boolean getDisableUseInClipsClientCache() {
        return disableUseInClipsClientCache;
    }

    public void setDisableUseInClipsClientCache(Boolean disableUseInClipsClientCache) {
        this.disableUseInClipsClientCache = disableUseInClipsClientCache;
    }

    public Object getContentAppreciationInfo() {
        return contentAppreciationInfo;
    }

    public void setContentAppreciationInfo(Object contentAppreciationInfo) {
        this.contentAppreciationInfo = contentAppreciationInfo;
    }

}