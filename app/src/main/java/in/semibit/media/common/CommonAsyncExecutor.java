package in.semibit.media.common;

import com.semibit.ezandroidutils.EzUtils;

public class CommonAsyncExecutor {
    private static final ThreadGroup group = new ThreadGroup("CommonAsyncExecutor");
    public static void execute(Runnable e){
        new Thread(group, e, "CommonAsyncExecutor-"+ EzUtils.randomInt(1,100), 48 * 1040 * 1024).start();
    }
}
