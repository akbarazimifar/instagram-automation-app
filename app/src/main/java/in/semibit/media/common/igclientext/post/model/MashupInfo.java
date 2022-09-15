package in.semibit.media.common.igclientext.post.model;


import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class MashupInfo {

    @SerializedName("mashups_allowed")
    @Expose
    private Boolean mashupsAllowed;
    @SerializedName("can_toggle_mashups_allowed")
    @Expose
    private Boolean canToggleMashupsAllowed;
    @SerializedName("has_been_mashed_up")
    @Expose
    private Boolean hasBeenMashedUp;
    @SerializedName("formatted_mashups_count")
    @Expose
    private Object formattedMashupsCount;
    @SerializedName("original_media")
    @Expose
    private Object originalMedia;
    @SerializedName("non_privacy_filtered_mashups_media_count")
    @Expose
    private Integer nonPrivacyFilteredMashupsMediaCount;
    @SerializedName("mashup_type")
    @Expose
    private Object mashupType;
    @SerializedName("is_creator_requesting_mashup")
    @Expose
    private Boolean isCreatorRequestingMashup;
    @SerializedName("has_nonmimicable_additional_audio")
    @Expose
    private Object hasNonmimicableAdditionalAudio;

    public Boolean getMashupsAllowed() {
        return mashupsAllowed;
    }

    public void setMashupsAllowed(Boolean mashupsAllowed) {
        this.mashupsAllowed = mashupsAllowed;
    }

    public Boolean getCanToggleMashupsAllowed() {
        return canToggleMashupsAllowed;
    }

    public void setCanToggleMashupsAllowed(Boolean canToggleMashupsAllowed) {
        this.canToggleMashupsAllowed = canToggleMashupsAllowed;
    }

    public Boolean getHasBeenMashedUp() {
        return hasBeenMashedUp;
    }

    public void setHasBeenMashedUp(Boolean hasBeenMashedUp) {
        this.hasBeenMashedUp = hasBeenMashedUp;
    }

    public Object getFormattedMashupsCount() {
        return formattedMashupsCount;
    }

    public void setFormattedMashupsCount(Object formattedMashupsCount) {
        this.formattedMashupsCount = formattedMashupsCount;
    }

    public Object getOriginalMedia() {
        return originalMedia;
    }

    public void setOriginalMedia(Object originalMedia) {
        this.originalMedia = originalMedia;
    }

    public Integer getNonPrivacyFilteredMashupsMediaCount() {
        return nonPrivacyFilteredMashupsMediaCount;
    }

    public void setNonPrivacyFilteredMashupsMediaCount(Integer nonPrivacyFilteredMashupsMediaCount) {
        this.nonPrivacyFilteredMashupsMediaCount = nonPrivacyFilteredMashupsMediaCount;
    }

    public Object getMashupType() {
        return mashupType;
    }

    public void setMashupType(Object mashupType) {
        this.mashupType = mashupType;
    }

    public Boolean getIsCreatorRequestingMashup() {
        return isCreatorRequestingMashup;
    }

    public void setIsCreatorRequestingMashup(Boolean isCreatorRequestingMashup) {
        this.isCreatorRequestingMashup = isCreatorRequestingMashup;
    }

    public Object getHasNonmimicableAdditionalAudio() {
        return hasNonmimicableAdditionalAudio;
    }

    public void setHasNonmimicableAdditionalAudio(Object hasNonmimicableAdditionalAudio) {
        this.hasNonmimicableAdditionalAudio = hasNonmimicableAdditionalAudio;
    }

}