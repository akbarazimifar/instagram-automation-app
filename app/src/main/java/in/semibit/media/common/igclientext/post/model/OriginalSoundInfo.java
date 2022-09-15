package in.semibit.media.common.igclientext.post.model;


import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class OriginalSoundInfo {

    @SerializedName("audio_asset_id")
    @Expose
    private Long audioAssetId;
    @SerializedName("music_canonical_id")
    @Expose
    private Object musicCanonicalId;
    @SerializedName("progressive_download_url")
    @Expose
    private String progressiveDownloadUrl;
    @SerializedName("dash_manifest")
    @Expose
    private String dashManifest;
    @SerializedName("ig_artist")
    @Expose
    private IgArtist igArtist;
    @SerializedName("should_mute_audio")
    @Expose
    private Boolean shouldMuteAudio;
    @SerializedName("original_media_id")
    @Expose
    private Long originalMediaId;
    @SerializedName("hide_remixing")
    @Expose
    private Boolean hideRemixing;
    @SerializedName("duration_in_ms")
    @Expose
    private Integer durationInMs;
    @SerializedName("time_created")
    @Expose
    private Integer timeCreated;
    @SerializedName("original_audio_title")
    @Expose
    private String originalAudioTitle;

    @SerializedName("allow_creator_to_rename")
    @Expose
    private Boolean allowCreatorToRename;
    @SerializedName("can_remix_be_shared_to_fb")
    @Expose
    private Boolean canRemixBeSharedToFb;
    @SerializedName("formatted_clips_media_count")
    @Expose
    private Object formattedClipsMediaCount;
    @SerializedName("audio_parts")
    @Expose
    private List<Object> audioParts = null;
    @SerializedName("is_explicit")
    @Expose
    private Boolean isExplicit;
    @SerializedName("original_audio_subtype")
    @Expose
    private String originalAudioSubtype;
    @SerializedName("is_audio_automatically_attributed")
    @Expose
    private Boolean isAudioAutomaticallyAttributed;

    public Long getAudioAssetId() {
        return audioAssetId;
    }

    public void setAudioAssetId(Long audioAssetId) {
        this.audioAssetId = audioAssetId;
    }

    public Object getMusicCanonicalId() {
        return musicCanonicalId;
    }

    public void setMusicCanonicalId(Object musicCanonicalId) {
        this.musicCanonicalId = musicCanonicalId;
    }

    public String getProgressiveDownloadUrl() {
        return progressiveDownloadUrl;
    }

    public void setProgressiveDownloadUrl(String progressiveDownloadUrl) {
        this.progressiveDownloadUrl = progressiveDownloadUrl;
    }

    public String getDashManifest() {
        return dashManifest;
    }

    public void setDashManifest(String dashManifest) {
        this.dashManifest = dashManifest;
    }

    public IgArtist getIgArtist() {
        return igArtist;
    }

    public void setIgArtist(IgArtist igArtist) {
        this.igArtist = igArtist;
    }

    public Boolean getShouldMuteAudio() {
        return shouldMuteAudio;
    }

    public void setShouldMuteAudio(Boolean shouldMuteAudio) {
        this.shouldMuteAudio = shouldMuteAudio;
    }

    public Long getOriginalMediaId() {
        return originalMediaId;
    }

    public void setOriginalMediaId(Long originalMediaId) {
        this.originalMediaId = originalMediaId;
    }

    public Boolean getHideRemixing() {
        return hideRemixing;
    }

    public void setHideRemixing(Boolean hideRemixing) {
        this.hideRemixing = hideRemixing;
    }

    public Integer getDurationInMs() {
        return durationInMs;
    }

    public void setDurationInMs(Integer durationInMs) {
        this.durationInMs = durationInMs;
    }

    public Integer getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Integer timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getOriginalAudioTitle() {
        return originalAudioTitle;
    }

    public void setOriginalAudioTitle(String originalAudioTitle) {
        this.originalAudioTitle = originalAudioTitle;
    }


    public Boolean getAllowCreatorToRename() {
        return allowCreatorToRename;
    }

    public void setAllowCreatorToRename(Boolean allowCreatorToRename) {
        this.allowCreatorToRename = allowCreatorToRename;
    }

    public Boolean getCanRemixBeSharedToFb() {
        return canRemixBeSharedToFb;
    }

    public void setCanRemixBeSharedToFb(Boolean canRemixBeSharedToFb) {
        this.canRemixBeSharedToFb = canRemixBeSharedToFb;
    }

    public Object getFormattedClipsMediaCount() {
        return formattedClipsMediaCount;
    }

    public void setFormattedClipsMediaCount(Object formattedClipsMediaCount) {
        this.formattedClipsMediaCount = formattedClipsMediaCount;
    }

    public List<Object> getAudioParts() {
        return audioParts;
    }

    public void setAudioParts(List<Object> audioParts) {
        this.audioParts = audioParts;
    }

    public Boolean getIsExplicit() {
        return isExplicit;
    }

    public void setIsExplicit(Boolean isExplicit) {
        this.isExplicit = isExplicit;
    }

    public String getOriginalAudioSubtype() {
        return originalAudioSubtype;
    }

    public void setOriginalAudioSubtype(String originalAudioSubtype) {
        this.originalAudioSubtype = originalAudioSubtype;
    }

    public Boolean getIsAudioAutomaticallyAttributed() {
        return isAudioAutomaticallyAttributed;
    }

    public void setIsAudioAutomaticallyAttributed(Boolean isAudioAutomaticallyAttributed) {
        this.isAudioAutomaticallyAttributed = isAudioAutomaticallyAttributed;
    }

}