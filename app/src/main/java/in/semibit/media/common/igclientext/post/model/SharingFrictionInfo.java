
package in.semibit.media.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class SharingFrictionInfo {

    @SerializedName("should_have_sharing_friction")
    @Expose
    private Boolean shouldHaveSharingFriction;
    @SerializedName("bloks_app_url")
    @Expose
    private Object bloksAppUrl;
    @SerializedName("sharing_friction_payload")
    @Expose
    private Object sharingFrictionPayload;

    public Boolean getShouldHaveSharingFriction() {
        return shouldHaveSharingFriction;
    }

    public void setShouldHaveSharingFriction(Boolean shouldHaveSharingFriction) {
        this.shouldHaveSharingFriction = shouldHaveSharingFriction;
    }

    public Object getBloksAppUrl() {
        return bloksAppUrl;
    }

    public void setBloksAppUrl(Object bloksAppUrl) {
        this.bloksAppUrl = bloksAppUrl;
    }

    public Object getSharingFrictionPayload() {
        return sharingFrictionPayload;
    }

    public void setSharingFrictionPayload(Object sharingFrictionPayload) {
        this.sharingFrictionPayload = sharingFrictionPayload;
    }

}
