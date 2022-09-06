package in.semibit.instadp.common;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

import in.semibit.instadp.MainActivity;

public abstract class JSInterface {
    @JavascriptInterface
    public abstract void callback(String body);
}
