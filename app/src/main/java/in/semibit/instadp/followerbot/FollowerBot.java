package in.semibit.instadp.followerbot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import com.github.instagram4j.instagram4j.IGClient;

import java.util.Timer;
import java.util.TimerTask;

import in.semibit.instadp.common.AdvancedWebView;
import in.semibit.instadp.common.GenricDataCallback;
import in.semibit.instadp.common.JSInterface;

public class FollowerBot {

    IGClient client;
    GenricDataCallback logger;
    GenricDataCallback onPageFinished;

    public FollowerBot(IGClient client, GenricDataCallback logger) {
        this.client = client;
        this.logger = logger;
    }

    public void initializeWebView(AdvancedWebView webview, Activity context) {

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

    public void follow(String igUser, AdvancedWebView webview, Activity context, GenricDataCallback onFollowCompleted) {

        String url = "https://www.instagram.com/" + igUser + "/";
        // initializing following of user
        logger.onStart("INT FLW " + igUser);
        initializeWebView(webview, context);

        onPageFinished = new GenricDataCallback() {
            @Override
            public void onStart(String s) {
                if (s.contains(url)) {
                    pressFollowButton(igUser, webview, onFollowCompleted);
                }
            }
        };
        webview.loadUrl(url);
    }

    public void pressFollowButton(String username, AdvancedWebView webview, GenricDataCallback onFollowCompleted) {

        logger.onStart("WAT FLW " + username);
        String clickOnFollow = "\n" +
                "function find() {\n" +
                "  var aTags = document.getElementsByTagName(\"button\");\n" +
                "  var searchText = \"Follow\";\n" +
                "  var found;\n" +
                "  console.log('Found matching ' + aTags.length);" +
                "  for (var i = 0; i < aTags.length; i) {\n" +
                "    let text=aTags[i].textContent;" +
                "    console.log(text);" +
                "    if (text == searchText) {\n" +
                "      found = aTags[i];\n" +
                "      found.click();\n" +
                "      android.callback(found); \n" +
                "      break;\n" +
                "    }\n" +
                "  }\n" +
                "  if (!found)\n" +
                "    console.log('not found');\n" +
                "  else\n" +
                "    console.log('Found')\n" +
                "}";
        clickOnFollow = clickOnFollow + "\n\nvar vvv=0;;setInterval(()=>{console.log('Searching... '+(vvv++));" +
                "try{find();}catch(e){console.log(e);};" +
                ";},3000)";
        webview.addJavascriptInterface(new JSInterface() {
            @Override
            public void callback(String body) {
                if (body.contains("done")) {
                    try {
                        new Handler(webview.getWebViewLooper()).postDelayed(() -> {
                            onFollowCompleted.onStart(username);
                        }, 5000);
                    } catch (Exception e) {
                        logger.onStart("ERR " + e.getMessage());
                    }
                }
            }
        }, "android");

        webview.evaluateJavascript(clickOnFollow,s->{});
//        webview.loadUrl("javascript:(function(){document.getElementById('password').value = 'sb14november';})()");

    }
}
