
package in.semibit.instadp.common.igclientext.post.model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class PostInfoModel {
    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    String jsonResponse;

    @SerializedName("items")
    @Expose
    private List<PostItem> items = null;
    @SerializedName("num_results")
    @Expose
    private Integer numResults;
    @SerializedName("more_available")
    @Expose
    private Boolean moreAvailable;
    @SerializedName("auto_load_more_enabled")
    @Expose
    private Boolean autoLoadMoreEnabled;
    @SerializedName("status")
    @Expose
    private String status;

    public List<PostItem> getItems() {
        return items;
    }

    public void setItems(List<PostItem> postItems) {
        this.items = postItems;
    }

    public Integer getNumResults() {
        return numResults;
    }

    public void setNumResults(Integer numResults) {
        this.numResults = numResults;
    }

    public Boolean getMoreAvailable() {
        return moreAvailable;
    }

    public void setMoreAvailable(Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }

    public Boolean getAutoLoadMoreEnabled() {
        return autoLoadMoreEnabled;
    }

    public void setAutoLoadMoreEnabled(Boolean autoLoadMoreEnabled) {
        this.autoLoadMoreEnabled = autoLoadMoreEnabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
