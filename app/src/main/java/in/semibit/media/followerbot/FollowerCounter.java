package in.semibit.media.followerbot;

import in.semibit.media.common.database.IdentifiedModel;

public class FollowerCounter implements IdentifiedModel {
    String id;
    int count;
    String tenant = "semibitmedia";

    public FollowerCounter() {
    }

    public FollowerCounter(String id, int count) {
        this.id = id;
        this.count = count;
    }

    @Override
    public String getId() {
        return id;
    }
}
