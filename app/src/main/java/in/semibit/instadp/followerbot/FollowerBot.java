package in.semibit.instadp.followerbot;

import com.github.instagram4j.instagram4j.IGClient;

import in.semibit.instadp.common.GenricDataCallback;

public class FollowerBot {

    IGClient client;
    GenricDataCallback logger;

    public FollowerBot(IGClient client,GenricDataCallback logger) {
        this.client = client;
        this.logger = logger;
    }

    public void follow(String igUser){
        if(client.isLoggedIn()){
            logger.onStart("Please login to continue");
            return;
        }

    }
}
