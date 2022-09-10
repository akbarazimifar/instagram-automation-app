package in.semibit.media.common;

import android.webkit.JavascriptInterface;

public abstract class JSInterface {
    @JavascriptInterface
    public abstract void callback(String body);
}
