package in.semibit.media.common;

import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;

import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class LogsViewModel {
    private static MutableLiveData<List<Pair<Instant, String>>> liveLogData =
            new MutableLiveData<>(new ArrayList<>(getInitialData()));

    private static List<Pair<Instant, String>> getInitialData() {
        List<Pair<Instant, String>> initialData = new ArrayList<>();
        initialData.add(Pair.create(Instant.now(), "Initiated background logger"));
        return initialData;
    }

    public synchronized static MutableLiveData<List<Pair<Instant, String>>> getLiveLogData() {
        if (liveLogData == null) {
            liveLogData = new MutableLiveData<>();
            liveLogData.postValue(getInitialData());
        }
        return liveLogData;
    }

    public synchronized static void addToLog(String log) {

        MutableLiveData<List<Pair<Instant, String>>> logs = getLiveLogData();
        List<Pair<Instant, String>> logList = logs.getValue();
        if (logList == null) {
            logList = new ArrayList<>(getInitialData());
        }
        logList.add(Pair.create(Instant.now(), "" + log));
        getLiveLogData().postValue(logs.getValue());
    }

    public synchronized static List<Pair<Instant, String>> filterAfter(Instant alreadyHaveLogsTill) {
        MutableLiveData<List<Pair<Instant, String>>> logs = getLiveLogData();
        List<Pair<Instant, String>> logList = logs.getValue();
        if (logList == null) {
            logList = new ArrayList<>(getInitialData());
        }
        if (alreadyHaveLogsTill != null) {
            logList = logList.stream().filter(log -> log.first.isAfter(alreadyHaveLogsTill))
                    .collect(Collectors.toList());
        }
        return logList;
    }

    public static String formattedLog(Pair<Instant, String> stringPair) {
        Date date = Date.from(stringPair.first);
        String dateFormatted = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
        return "\n" + dateFormatted + "\n" + stringPair.second + "\n";
    }
}
