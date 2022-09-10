
package in.semibit.media.common.igclientext.post.model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class FanClubInfo {

    @SerializedName("fan_club_id")
    @Expose
    private Object fanClubId;
    @SerializedName("fan_club_name")
    @Expose
    private Object fanClubName;

    public Object getFanClubId() {
        return fanClubId;
    }

    public void setFanClubId(Object fanClubId) {
        this.fanClubId = fanClubId;
    }

    public Object getFanClubName() {
        return fanClubName;
    }

    public void setFanClubName(Object fanClubName) {
        this.fanClubName = fanClubName;
    }

}
