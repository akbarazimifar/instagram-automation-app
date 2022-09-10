
package in.semibit.media.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class CommentInformTreatment {

    @SerializedName("should_have_inform_treatment")
    @Expose
    private Boolean shouldHaveInformTreatment;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("url")
    @Expose
    private Object url;
    @SerializedName("action_type")
    @Expose
    private Object actionType;

    public Boolean getShouldHaveInformTreatment() {
        return shouldHaveInformTreatment;
    }

    public void setShouldHaveInformTreatment(Boolean shouldHaveInformTreatment) {
        this.shouldHaveInformTreatment = shouldHaveInformTreatment;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getUrl() {
        return url;
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public Object getActionType() {
        return actionType;
    }

    public void setActionType(Object actionType) {
        this.actionType = actionType;
    }

}
