package in.semibit.media.videoprocessor;

import android.content.Context;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.semibit.ezandroidutils.EzUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.common.GenricDataCallback;
import in.semibit.media.common.Insta4jClient;

public class VideoMerger {

    GenricDataCallback onLog;
    Context context;

    public VideoMerger(Context context) {
        this.context = context;
    }

    public void setOnLog(GenricDataCallback onLog) {
        this.onLog = onLog;
    }


    public CompletableFuture<File> merge(String fileName, List<File> mp4Files) {
        if (onLog == null) {
            onLog = EzUtils::l;
        }
        File outputFile = new File(Insta4jClient.root, fileName);
        File tempDir = new File(Insta4jClient.root, "temp");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        CompletableFuture<File> onRes = new CompletableFuture<>();
        try {

            CompletableFuture<Boolean>[] tsProcessorsFutures = new CompletableFuture[mp4Files.size()];
            List<String>  temps = new ArrayList<>();
            for (int i = 0; i < mp4Files.size(); i++) {
                File tsFile = new File(tempDir.getAbsoluteFile(), mp4Files.get(i).getName() + ".ts");
                temps.add(tsFile.getAbsolutePath());
                tsProcessorsFutures[i] = run("-i " + mp4Files.get(i).getAbsolutePath() + " -c copy -bsf:v h264_mp4toannexb -f mpegts -y " + tsFile.getAbsolutePath());
            }
            CompletableFuture.allOf(tsProcessorsFutures).join();
            run("-i \"concat:"+ String.join("|", temps) +"\" -c copy -bsf:a aac_adtstoasc -y "+outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            onRes.completeExceptionally(e);
        }

        return onRes;
    }

    public CompletableFuture<Boolean> run(String cmd) {
        CompletableFuture<Boolean> onRun = new CompletableFuture<>();
        FFmpegSession session = FFmpegKit.executeAsync(cmd, session1 -> {
            if (ReturnCode.isSuccess(session1.getReturnCode())) {
                Log.d("VideoMerger", String.format("Command success with state %s and rc %s", session1.getState(), session1.getReturnCode()));
                onRun.complete(true);
            } else if (ReturnCode.isCancel(session1.getReturnCode())) {
                onRun.complete(false);
            } else {
                onRun.complete(false);
                Log.d("VideoMerger", String.format("Command failed with state %s and rc %s.%s", session1.getState(), session1.getReturnCode(), session1.getFailStackTrace()));
            }
        }, log -> EzUtils.l(log.getMessage()), statistics -> EzUtils.l(statistics.toString()));

        return onRun;
    }

    public static void load(Context context) {

    }
}
