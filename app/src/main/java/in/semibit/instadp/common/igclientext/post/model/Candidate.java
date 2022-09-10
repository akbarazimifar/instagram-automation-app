
package in.semibit.instadp.common.igclientext.post.model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Candidate {

    @SerializedName("width")
    @Expose
    private Integer width;
    @SerializedName("height")
    @Expose
    private Integer height;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("scans_profile")
    @Expose
    private String scansProfile;
    @SerializedName("estimated_scans_sizes")
    @Expose
    private List<Integer> estimatedScansSizes = null;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScansProfile() {
        return scansProfile;
    }

    public void setScansProfile(String scansProfile) {
        this.scansProfile = scansProfile;
    }

    public List<Integer> getEstimatedScansSizes() {
        return estimatedScansSizes;
    }

    public void setEstimatedScansSizes(List<Integer> estimatedScansSizes) {
        this.estimatedScansSizes = estimatedScansSizes;
    }

}
