package in.semibit.media.followerbot;

import com.semibit.ezandroidutils.EzUtils;

import java.util.ArrayList;
import java.util.List;

public class FollowerUtil {

    public static List<FollowUserModel> getUsersThatDontFollowMe(List<FollowUserModel> usersFollowingMe,
                                                                 List<FollowUserModel> usersIAmFollowing){

        List<FollowUserModel> usersToBeUnFollowed = new ArrayList<>();
        List<FollowUserModel> mutualUsers = new ArrayList<>();

        for(FollowUserModel userModel:usersIAmFollowing){
            if(usersFollowingMe.stream().anyMatch(uFm-> uFm.getId().equals(userModel.getId()))){
                mutualUsers.add(userModel);
            }
            else {
                usersToBeUnFollowed.add(userModel);
            }
        }

        EzUtils.log("FollowerBot","My Followers="+usersFollowingMe.size()+"\n"+
                "I Follow="+usersIAmFollowing.size()+"\n"+
                "Who didnt follow back="+usersToBeUnFollowed.size()+"\n"+
                "Mutual="+mutualUsers.size()+"\n");

        return usersToBeUnFollowed;
    }
}
