package in.semibit.instadp.followerbot;

import android.webkit.WebView;

import com.github.instagram4j.instagram4j.IGClient;

import in.semibit.instadp.common.GenricDataCallback;

public class FollowerBot {

    IGClient client;
    GenricDataCallback logger;

    public FollowerBot(IGClient client,GenricDataCallback logger) {
        this.client = client;
        this.logger = logger;
    }

    public void follow(String igUser, WebView webView){

        String url = "https://www.instagram.com/"+igUser+"/";


    }
}
