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

    public static <T>List<List<T>> chopIntoParts( final List<T> ls, final int iParts )
    {
        final List<List<T>> lsParts = new ArrayList<List<T>>();
        final int iChunkSize = ls.size() / iParts;
        int iLeftOver = ls.size() % iParts;
        int iTake = iChunkSize;

        for( int i = 0, iT = ls.size(); i < iT; i += iTake )
        {
            if( iLeftOver > 0 )
            {
                iLeftOver--;

                iTake = iChunkSize + 1;
            }
            else
            {
                iTake = iChunkSize;
            }

            lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
        }

        return lsParts;
    }
}
