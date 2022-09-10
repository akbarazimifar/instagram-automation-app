
package in.semibit.media.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Caption {

    @SerializedName("pk")
    @Expose
    private Long pk;
    @SerializedName("user_id")
    @Expose
    private Long userId;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("created_at")
    @Expose
    private Integer createdAt;
    @SerializedName("created_at_utc")
    @Expose
    private Integer createdAtUtc;
    @SerializedName("content_type")
    @Expose
    private String contentType;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("bit_flags")
    @Expose
    private Integer bitFlags;
    @SerializedName("did_report_as_spam")
    @Expose
    private Boolean didReportAsSpam;
    @SerializedName("share_enabled")
    @Expose
    private Boolean shareEnabled;
    @SerializedName("user")
    @Expose
    private User__1 user;
    @SerializedName("is_covered")
    @Expose
    private Boolean isCovered;
    @SerializedName("media_id")
    @Expose
    private Long mediaId;
    @SerializedName("private_reply_status")
    @Expose
    private Integer privateReplyStatus;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedAtUtc() {
        return createdAtUtc;
    }

    public void setCreatedAtUtc(Integer createdAtUtc) {
        this.createdAtUtc = createdAtUtc;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBitFlags() {
        return bitFlags;
    }

    public void setBitFlags(Integer bitFlags) {
        this.bitFlags = bitFlags;
    }

    public Boolean getDidReportAsSpam() {
        return didReportAsSpam;
    }

    public void setDidReportAsSpam(Boolean didReportAsSpam) {
        this.didReportAsSpam = didReportAsSpam;
    }

    public Boolean getShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(Boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

    public User__1 getUser() {
        return user;
    }

    public void setUser(User__1 user) {
        this.user = user;
    }

    public Boolean getIsCovered() {
        return isCovered;
    }

    public void setIsCovered(Boolean isCovered) {
        this.isCovered = isCovered;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getPrivateReplyStatus() {
        return privateReplyStatus;
    }

    public void setPrivateReplyStatus(Integer privateReplyStatus) {
        this.privateReplyStatus = privateReplyStatus;
    }

}
