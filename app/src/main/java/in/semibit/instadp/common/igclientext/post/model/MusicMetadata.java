
package in.semibit.instadp.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class MusicMetadata {

    @SerializedName("music_canonical_id")
    @Expose
    private String musicCanonicalId;
    @SerializedName("audio_type")
    @Expose
    private Object audioType;
    @SerializedName("music_info")
    @Expose
    private Object musicInfo;
    @SerializedName("original_sound_info")
    @Expose
    private Object originalSoundInfo;
    @SerializedName("pinned_media_ids")
    @Expose
    private Object pinnedMediaIds;

    public String getMusicCanonicalId() {
        return musicCanonicalId;
    }

    public void setMusicCanonicalId(String musicCanonicalId) {
        this.musicCanonicalId = musicCanonicalId;
    }

    public Object getAudioType() {
        return audioType;
    }

    public void setAudioType(Object audioType) {
        this.audioType = audioType;
    }

    public Object getMusicInfo() {
        return musicInfo;
    }

    public void setMusicInfo(Object musicInfo) {
        this.musicInfo = musicInfo;
    }

    public Object getOriginalSoundInfo() {
        return originalSoundInfo;
    }

    public void setOriginalSoundInfo(Object originalSoundInfo) {
        this.originalSoundInfo = originalSoundInfo;
    }

    public Object getPinnedMediaIds() {
        return pinnedMediaIds;
    }

    public void setPinnedMediaIds(Object pinnedMediaIds) {
        this.pinnedMediaIds = pinnedMediaIds;
    }

}
