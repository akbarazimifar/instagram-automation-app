package in.semibit.instadp.followerbot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

import com.github.instagram4j.instagram4j.IGClient;

import in.semibit.instadp.common.AdvancedWebView;
import in.semibit.instadp.common.GenricDataCallback;

public class FollowerBot {

    IGClient client;
    GenricDataCallback logger;

    public FollowerBot(IGClient client,GenricDataCallback logger) {
        this.client = client;
        this.logger = logger;
    }

    public void follow(String igUser, AdvancedWebView webview, Activity context){

        String url = "https://www.instagram.com/"+igUser+"/";
        // initializing following of user
        logger.onStart("INT FLW "+igUser);


        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("FollowerBot", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webview.setListener(context, new AdvancedWebView.Listener() {
            @Override
            public void onPageStarted(String url, Bitmap favicon) {
                Log.e("FollowerBot", url);
                if (url.equals("https://www.instagram.com/")) {
                    logger.onStart("Logged In");
                    webview.setVisibility(View.GONE);
                }
                else if(url.contains("/accounts/login")){
                    logger.onStart("Login Required");
                    webview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(String url) {
                Log.e("FollowerBot","Page loaded "+url);
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
                Log.e("FollowerBot","Page Error "+description);

            }

            @Override
            public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

            }

            @Override
            public void onExternalPageRequest(String url) {

            }
        });

        webview.loadUrl(url);


    }
}
