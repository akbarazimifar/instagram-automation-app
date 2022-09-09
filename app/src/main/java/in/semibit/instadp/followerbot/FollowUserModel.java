package in.semibit.instadp.followerbot;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class FollowUserModel {

    @Id
    public long id;

    public String userName;

    public int followUserState;

    public boolean isUserFollowingMe;

    public long followDate;

    public long unfollowDate;

    public long waitTillFollowBackDate;

}
