package in.semibit.media.followerbot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.github.instagram4j.instagram4j.IGClient;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.GenricDataCallback;

public class FollowerBot {

    IGClient client;
    GenricDataCallback logger;
    GenricDataCallback onPageFinished;

    public FollowerBot(IGClient client, GenricDataCallback logger) {
        this.client = client;
        this.logger = logger;
    }

    public void initializeWebView(AdvancedWebView webview, Activity context) {

        webview.setListener(context, new AdvancedWebView.Listener() {
            @Override
            public void onPageStarted(String url, Bitmap favicon) {
                Log.e("FollowerBot", url);
                if (url.equals("https://www.instagram.com/")) {
                    logger.onStart("Logged In");
                    webview.setVisibility(View.GONE);
                } else if (url.contains("/accounts/login")) {
                    logger.onStart("Login Required");
                    webview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(String url) {
                Log.e("FollowerBot", "Page loaded " + url);
                onPageFinished.onStart(url);
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
                Log.e("FollowerBot", "Page Error " + description);

            }

            @Override
            public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

            }

            @Override
            public void onExternalPageRequest(String url) {

            }
        });
    }

    public void followUnfollow(String igUser,boolean isDoUnfollow, AdvancedWebView webview, Activity context, GenricDataCallback onFollowCompleted) {

        String url = "https://www.instagram.com/" + igUser + "/";
        // initializing following of user
        logger.onStart("INT FLW " + igUser);
        initializeWebView(webview, context);

        onPageFinished = new GenricDataCallback() {
            @Override
            public void onStart(String s) {
                if (s.contains(url)) {
                    pressFollowButton(igUser,isDoUnfollow, webview, onFollowCompleted);
                }
            }
        };
        webview.loadUrl(url);
    }

    public void pressFollowButton(String username,boolean isDoUnfollow, AdvancedWebView webview, GenricDataCallback onFollowCompleted) {
        WebView.setWebContentsDebuggingEnabled(true);

        logger.onStart("WAT FLW " + username);
        String clickOnFollow = "\n" +
                "function find() {\n" +
                "  let btns = Array.from(document.querySelectorAll('button')).find(el => el.textContent === 'Follow');" +
                "  " +
                "   " +
                "if(btns){" +
                "btns.click();" +
//                "clearInterval(timer);" +
//                "setTimeout(()=>{console.log('followcompleted');},5000);" +
                "" +
                "}" +
                "" +
                "  let msgs = Array.from(document.querySelectorAll('button')).find(el => el.textContent === 'Requested' || el.textContent === 'Message');" +
                "  " +
                "   " +
                "if(msgs){" +
//                "msgs.click();" +
                "clearInterval(timer);" +
                "console.log('followcompleted');" +
                "" +
                "}" +
                "}";

        if(isDoUnfollow){
            clickOnFollow = "\n" +
                    "function find() {\n" +
                    "  let btns = Array.from(document.querySelectorAll('button')).find(el => el.textContent === 'Requested' || el.textContent === 'Unfollow');" +
                    "  " +
                    "   " +
                    "if(btns){" +
                    "btns.click();" +
//                "clearInterval(timer);" +
//                "setTimeout(()=>{console.log('followcompleted');},5000);" +
                    "" +
                    "}" +
                    "  let confirm = Array.from(document.querySelectorAll('button')).find(el => el.textContent === 'Unfollow');" +
                    "  " +
                    "   " +
                    "if(confirm){" +
                    "confirm.click();" +
//                "clearInterval(timer);" +
//                "setTimeout(()=>{console.log('followcompleted');},5000);" +
                    "" +
                    "}" +
                    "" +
                    "  let folowbtn = Array.from(document.querySelectorAll('button')).find(el => el.textContent === 'Follow');" +
                    "  " +
                    "   " +
                    "if(folowbtn){" +
//                "msgs.click();" +
                    "clearInterval(timer);" +
                    "console.log('unconnectcompleted');" +
                    "" +
                    "}" +
                    "}";
        }
        clickOnFollow = clickOnFollow + "\n\nvar vvv=0;let timer = setInterval(()=>{console.log('Searching... '+(vvv++));" +
                "try{find();}catch(e){console.log(e);};" +
                ";},1000)";

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("FollowerBot", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                if (consoleMessage.message().contains("followcompleted")) {
                    try {
                        logger.onStart("FLW COMPLETE");
                        onFollowCompleted.onStart(username);
                    } catch (Exception e) {
                        logger.onStart("ERR " + e.getMessage());
                    }
                }
                if (consoleMessage.message().contains("unconnectcompleted")) {
                    try {
                        logger.onStart("UFW COMPLETE");
                        onFollowCompleted.onStart(username);
                    } catch (Exception e) {
                        logger.onStart("ERR " + e.getMessage());
                    }
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webview.evaluateJavascript(clickOnFollow, s -> {
        });
    }

    public static class JSIFollowListener {
        GenricDataCallback callback;

        @JavascriptInterface
        public void callback(String body) {
            callback.onStart(body);
        }
    }
}
