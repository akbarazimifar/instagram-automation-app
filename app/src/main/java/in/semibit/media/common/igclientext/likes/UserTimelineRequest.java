package in.semibit.media.common.igclientext.likes;

import com.github.instagram4j.instagram4j.IGClient;
import com.semibit.ezandroidutils.EzUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import in.semibit.media.common.LogsViewModel;
import in.semibit.media.common.database.GenericCompletableFuture;
import in.semibit.media.common.igclientext.IGrequestHelper;
import in.semibit.media.common.igclientext.post.model.PostItem;
import in.semibit.media.followerbot.FollowUserModel;

public class UserTimelineRequest {

    IGrequestHelper iGrequestHelper;
    IGClient client;

    public UserTimelineRequest(IGClient client) {
        this.client = client;
        this.iGrequestHelper = new IGrequestHelper(client);
    }

    public GenericCompletableFuture<List<PostItem>> getPosts(FollowUserModel user, int countPosts) {
        GenericCompletableFuture<List<PostItem>> future = new GenericCompletableFuture<>();
        String response = iGrequestHelper.doIGGet("/api/v1/feed/user/" + user.getId() + "/?exclude_comment=true&count=" + (countPosts), null);

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray("items");
            EzUtils.JSONParser<PostItem> postItemJSONParser = new EzUtils.JSONParser<>();
            List<PostItem> postItems = postItemJSONParser.parseJSONArray(items.toString(), PostItem.class);
            if (postItems.size() > countPosts) {
                postItems = postItems.subList(0, countPosts);
            }
            future.complete(postItems);
            LogsViewModel.addToLog("Retrived timeline of " + user.userName + " OK " + postItems.size());

        } catch (Exception e) {
            future.completeExceptionally(e);
            LogsViewModel.addToLog("Retrived timeline of " + user.userName + " Fail " + response);
        }

        return future;
    }

}
