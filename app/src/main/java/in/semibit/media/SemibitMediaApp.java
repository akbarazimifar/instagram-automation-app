package in.semibit.media;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.semibit.ezandroidutils.App;

public class SemibitMediaApp extends App {

    public static final boolean TEST_MODE = false;
    public static final boolean FOLLOW_VIA_WEBUI = false;
    public static String CURRENT_TENANT = "semibitmedia";

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
