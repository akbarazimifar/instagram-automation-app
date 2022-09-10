package in.semibit.media.followerbot;

import in.semibit.media.common.database.IdentifiedModel;
import in.semibit.media.common.igclientext.post.model.User;

public class FollowUserModel implements IdentifiedModel {

    public String  id;

    public String userName;

    public FollowUserState followUserState;

    public FollowUserState isUserFollowingMeState;

    public long followDate;

    public long unfollowDate;

    public long waitTillFollowBackDate;


    public static FollowUserModel fromUserToBeFollowed(User user){
        FollowUserModel followUserModel = new FollowUserModel();
        followUserModel.id = String.valueOf(user.getPk());
        followUserModel.userName = user.getUsername();
        followUserModel.followDate = 0;
        followUserModel.unfollowDate = 0;
        followUserModel.waitTillFollowBackDate = 0;
        followUserModel.isUserFollowingMeState = FollowUserState.UNKNOWN;
        followUserModel.followUserState = FollowUserState.TO_BE_FOLLOWED;

        return followUserModel;
    }

    @Override
    public String getId() {
        return id;
    }
}
