package in.semibit.media;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.instagram4j.instagram4j.IGClient;
import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.semibit.ezandroidutils.EzUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.common.AdvancedWebView;
import in.semibit.media.common.CommonAsyncExecutor;
import in.semibit.media.common.Insta4jClient;
import in.semibit.media.postbot.BackgroundWorkerService;
import in.semibit.media.databinding.ActivityMainBinding;
import in.semibit.media.videoprocessor.VideoMerger;

public class MainActivity extends AppCompatActivity {

    static Context context;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        context = getApplicationContext();
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW}, 1234);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//      driver();

        binding.searchButton.setOnClickListener(v -> {

//
            if (binding.urlOrUsername.getText() == null || Strings.isEmptyOrWhitespace(binding.urlOrUsername.getText().toString())) {
                binding.conturlOrUsername.setError("Please enter post url");

                try {
                    String textToPaste = readFromClipboard(this);
                    if (textToPaste != null && textToPaste.contains("instagram.com/")) {
                        binding.urlOrUsername.setText(textToPaste.toString());
                        binding.conturlOrUsername.setError(null);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            binding.conturlOrUsername.setError(null);

            // if you want to get data using webview
            processUsingWebView(binding.urlOrUsername.getText().toString());

//
//            JSONObject sampel = new JSONObject();
//            try {
//                // working Post ID = CfZJkoIglBE
//                sampel.put("source_short_code","ChWoAW6DSM9");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            postInBackground(new File(Insta4jClient.root,"clip.mp4"), "Test Caption", "video",sampel);


        });

        binding.searchButton.postDelayed(()->{
//            binding.urlOrUsername.setText("https://www.instagram.com/p/CddSYo6jTk4/");
//            binding.searchButton.callOnClick();
        },3000);

        binding.searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isDownloadAndPost = false;
                view.callOnClick();
                return true;
            }
        });

        binding.refresh.setOnClickListener(e->{
            driver();
            binding.webview.setVisibility(View.VISIBLE);
        });

        binding.heaederText.setText("Reposter");
        binding.contBottom.setVisibility(View.VISIBLE);
        binding.startBot.setText("Follower Bot");
        binding.startBot.setOnClickListener(v -> {
            startActivity(new Intent(context, FollowerBotActivity.class));
        });
        binding.showHideBot.setOnClickListener(c->{
            getClient(true);
        });

        driver();
        getClient(false);


        CommonAsyncExecutor.execute(()->{
            VideoMerger.load(context);
        });

    }
    IGClient client;
    public CompletableFuture<IGClient> getClient(boolean forceLogin){
        if(client != null && !forceLogin){
            return CompletableFuture.completedFuture(client);
        }
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Init IG Client...");
        progressDialog.show();

        CompletableFuture<IGClient> igc = new CompletableFuture<>();
        CommonAsyncExecutor.execute(()->{
           client =  Insta4jClient.getClient(context,forceLogin, null);
           MainActivity.this.runOnUiThread(()->{
               progressDialog.hide();
           });
           igc.complete(client);

        });
        return igc;
    }

    public boolean isDownloadAndPost = true;

    public static String readFromClipboard(Activity context) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            return clipboard.getText().toString();
        } else {
            ClipboardManager clipboard = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            // Gets a content resolver instance
            ContentResolver cr = context.getContentResolver();

            // Gets the clipboard data from the clipboard
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null) {

                String text = null;
                String title = null;

                // Gets the first item from the clipboard data
                ClipData.Item item = clip.getItemAt(0);

                // Tries to get the item's contents as a URI pointing to a note
                Uri uri = item.getUri();

                // If the contents of the clipboard wasn't a reference to a
                // note, then
                // this converts whatever it is to text.
                if (text == null) {
                    text = coerceToText(context, item).toString();
                }

                return text;
            }
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public static CharSequence coerceToText(Context context, ClipData.Item item) {
        // If this Item has an explicit textual value, simply return that.
        CharSequence text = item.getText();
        if (text != null) {
            return text;
        }

        // If this Item has a URI value, try using that.
        Uri uri = item.getUri();
        if (uri != null) {

            // First see if the URI can be opened as a plain text stream
            // (of any sub-type). If so, this is the best textual
            // representation for it.
            FileInputStream stream = null;
            try {
                // Ask for a stream of the desired type.
                AssetFileDescriptor descr = context.getContentResolver()
                        .openTypedAssetFileDescriptor(uri, "text/*", null);
                stream = descr.createInputStream();
                InputStreamReader reader = new InputStreamReader(stream,
                        "UTF-8");

                // Got it... copy the stream into a local string and return it.
                StringBuilder builder = new StringBuilder(128);
                char[] buffer = new char[8192];
                int len;
                while ((len = reader.read(buffer)) > 0) {
                    builder.append(buffer, 0, len);
                }
                return builder.toString();

            } catch (FileNotFoundException e) {
                // Unable to open content URI as text... not really an
                // error, just something to ignore.

            } catch (IOException e) {
                // Something bad has happened.
                Log.w("ClippedData", "Failure loading text", e);
                return e.toString();

            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }

            // If we couldn't open the URI as a stream, then the URI itself
            // probably serves fairly well as a textual representation.
            return uri.toString();
        }

        // Finally, if all we have is an Intent, then we can just turn that
        // into text. Not the most user-friendly thing, but it's something.
        Intent intent = item.getIntent();
        if (intent != null) {
            return intent.toUri(Intent.URI_INTENT_SCHEME);
        }

        // Shouldn't get here, but just in case...
        return "";
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonParser jsonParser = new JsonParser();

    public class JS_INTERFACE {

        @JavascriptInterface
        public void scrape(String body) {
            if (body != null && body.contains("shortcode_media")) {
                Log.e("INSTAGRAMM", body);
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(body);
                        binding.log.setText(json.toString(4));
                        json.put("username",client.$username);
                        toast(MainActivity.this, "Posting using strategy : " + (isDownloadAndPost ? "Local Upload" : "Remote API"));
//                        if (isDownloadAndPost)
//                            getInfo(body,isDownloadAndPost);
//                        else
//                        {
//                            //post(body);
//                        }
                        getInfo(json.toString(),isDownloadAndPost);

                    } catch (Exception e) {
                        e.printStackTrace();
                        binding.log.setText(body);
                    }
                });
//                post(body);
            }
        }
    }

    public void processUsingWebView(String userOrLink) {
        String split[] = userOrLink.replace("https://", "").split("/");
        if (userOrLink.contains("instagram.com") && split.length > 2) {
            String shortCode = split[2];
            Log.e("INSTAGRAMMM", shortCode);

            String url = "https://www.instagram.com/graphql/query/?query_hash=9f8827793ef34641b2fb195d4d41151c&variables={%22shortcode%22:%22" + shortCode + "%22,%22child_comment_count%22:3,%22fetch_comment_count%22:40,%22parent_comment_count%22:24,%22has_threaded_comments%22:true}";
//            url = "https://www.instagram.com/graphql/query/?query_hash=9f8827793ef34641b2fb195d4d41151c&variables={%22shortcode%22:%22ChxSjsdD00g%22,%22child_comment_count%22:3,%22fetch_comment_count%22:40,%22parent_comment_count%22:24,%22has_threaded_comments%22:true}";
            binding.log.setText(url);


            String js = "\n" +
                    "try{\n" +
                    "\n" +
                    "    var xmlHttp = new XMLHttpRequest();\n" +
                    "let ural = '" + url + "';" +
                    "    xmlHttp.open( \"GET\", ural, false ); // false for synchronous request\n" +
                    "    xmlHttp.send( null );\n" +
                    "console.log(ural);" +
                    "android.scrape(ural);\n" +
                    "    console.log(xmlHttp.responseText);\n" +
                    "    let response  = xmlHttp.responseText;\n" +
                    "    android.scrape(response);\n" +

                    "}catch(e){\n" +
                    "    android.scrape(JSON.stringify(e));" +
                    "console.log('ERRRRR '+e.message+ ' '+ +JSON.stringify(e));\n" +

                    "}";

            //            binding.webview.loadUrl("https://www.instagram.com/graphql/query/?query_hash=9f8827793ef34641b2fb195d4d41151c&variables={%22shortcode%22:%22"+shortCode+"%22,%22child_comment_count%22:3,%22fetch_comment_count%22:40,%22parent_comment_count%22:24,%22has_threaded_comments%22:true}");
            binding.webview.evaluateJavascript(js, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.e("INSTAGRAM", s);
                }
            });

        }

//        curDownloadState = DownloadState.SEARCHING;
//
//        // download to lcoal
//        binding.contBottom.setVisibility(View.VISIBLE);
//        processScrapedData(new ScrappedData());

    }

    public void post(String bboy) {

        HashMap<String, String> ma = new HashMap<>();
        ma.put("body", bboy);
        AndroidNetworking.upload("https://pasteitapp.herokuapp.com/semibitmedia/instagram/handlePostInfo").addMultipartParameter(ma).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String body) {
                try {
                    JSONObject json = new JSONObject(body);
                    binding.log.setText(json.toString(4));
                } catch (Exception e) {
                    e.printStackTrace();
                    binding.log.setText(body);
                }
                binding.urlOrUsername.setText("");
                Toast.makeText(MainActivity.this, "Posted", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(ANError anError) {
                binding.log.setText(anError.getErrorBody());
                Toast.makeText(MainActivity.this, "Posted", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void downloadFile(JSONObject post) {
        String url = post.optString("sourcefileUrl");
        String source_short_code = post.optString("source_short_code");
        String tags = post.optString("tags");
        String text = post.optString("text");
        String media_type = post.optString("media_type");

        String fileName = source_short_code + (media_type.equals("video") ? ".mp4" : ".jpg");

        if (Strings.isEmptyOrWhitespace(url)) {
            url = post.optString("firebaseUrl");
        }

        File tmpDir = new File(Insta4jClient.root,"temp");
        AndroidNetworking.download(url, tmpDir.getAbsolutePath(), fileName)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        File file = new File(tmpDir, fileName);
                        if (file.exists()) {
                            postInBackground(file, text, media_type, post);
                        } else {
                            Toast.makeText(MainActivity.this, "Unable to get file", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

    public void postInBackground(File file, String caption, String mediaType, JSONObject post) {
        Intent intent = new Intent(getApplicationContext(), BackgroundWorkerService.class);
        intent.putExtra("file", file.getAbsolutePath());
        intent.putExtra("caption", caption);
        intent.putExtra("mediaType", mediaType);
        intent.putExtra("post", post.toString());

        startForegroundService(intent);
//        BackgroundWorkerService workerService = new BackgroundWorkerService();
//        workerService.context = MainActivity.this;
//        workerService.onStartCommand(intent, 0, 0);
    }

    public static void toast(Context context, String st) {
        if (context == null) {
            context = MainActivity.context;
        }
        try {
            Toast.makeText(context, st, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getInfo(String bboy,boolean isDownloadAndPost) {

        HashMap<String, String> ma = new HashMap<>();
        ma.put("body", bboy);
        AndroidNetworking.upload("https://pasteitapp.herokuapp.com/semibitmedia/instagram/getPostInfo").addMultipartParameter(ma).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String body) {
                try {
                    body = body.replace("sassyfull.memes", client.$username);
                    body = body.replace(getString(R.string.username), client.$username);
                    JSONObject json = new JSONObject(body);
                    binding.log.setText(json.toString(4));
                    Toast.makeText(MainActivity.this, "Downloading", Toast.LENGTH_LONG).show();

                    if(isDownloadAndPost){
                        downloadFile(json);
                    }
                    else {
                        EzUtils.copyToClipBoard("Caption",json.optString("text"),context);
                    }
                    binding.urlOrUsername.setText("");

                } catch (Exception e) {
                    e.printStackTrace();
                    binding.log.setText(body);
                }
            }

            @Override
            public void onError(ANError anError) {
                binding.log.setText(anError.getErrorBody());
            }
        });

    }


    public void driver() {

        binding.webview.loadUrl("https://www.instagram.com/accounts/edit/");
        binding.heaederText.setText("Logging In");
        binding.webview.addJavascriptInterface(new JS_INTERFACE(), "android");

        binding.webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        binding.webview.setListener(this, new AdvancedWebView.Listener() {
            @Override
            public void onPageStarted(String url, Bitmap favicon) {
//                Log.e("INSTAGRAMMM", url);

//                else if(url.contains("graphql/query/?query_hash=")){
//
//                }
            }

            @Override
            public void onPageFinished(String url) {
                if (url.contains("https://www.instagram.com/accounts/edit")) {
                    binding.heaederText.setText("Logged In");
                    binding.startBot.setText("Follower Bot");
                    binding.contBottom.setVisibility(View.VISIBLE);
                    binding.startBot.setOnClickListener(v -> {
                        startActivity(new Intent(context, FollowerBotActivity.class));
                    });
                    binding.webview.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {

            }

            @Override
            public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

            }

            @Override
            public void onExternalPageRequest(String url) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

}