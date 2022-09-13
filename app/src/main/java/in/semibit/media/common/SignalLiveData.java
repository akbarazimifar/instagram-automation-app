package in.semibit.media.common;

import android.content.BroadcastReceiver;
import android.content.Intent;

import androidx.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class SignalLiveData extends MutableLiveData<Set<String>> {

    private static SignalLiveData instance = new SignalLiveData();
    private static final Map<String, BroadcastReceiver> directObservers = new ConcurrentHashMap<>();

    private SignalLiveData() {
        super(getInitialData());
    }

    public static Set<String> getInitialData() {
        return new ConcurrentSkipListSet<>();
    }

    public synchronized static SignalLiveData getLiveLogData() {
        if (instance == null) {
            instance = new SignalLiveData();
            instance.postValue(getInitialData());
        }
        return instance;
    }

    public void addUnsafeObserver(String observerId, BroadcastReceiver onTrigger) {
        directObservers.put(observerId, onTrigger);
    }

    public void removeUnsafeObserver(String observerId) {
        directObservers.remove(observerId);
    }

    public synchronized void notifyObservers(Set<String> value) {
        try {
            value.forEach(singleSignal -> {
                directObservers.keySet().forEach(key -> {
                    try {
                        BroadcastReceiver val = directObservers.get(key);
                        Intent intent = new Intent();
                        intent.putExtra("jobName", singleSignal);
                        intent.setAction(key);
                        if (val != null)
                            val.onReceive(null, intent);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
            });
            value.clear();
        } catch (Exception e) {
            LogsViewModel.addToLog("Signal Error " + e.getMessage());
        }
    }

    public synchronized void postSingleValue(String singleSignal) {
        if (getValue() == null) {
            setValue(getInitialData());
        }
        getValue().add(singleSignal);
        postValue(getValue());
    }

    @Override
    public void postValue(Set<String> value) {
        super.postValue(value);
        notifyObservers(value);
    }

    @Override
    public void setValue(Set<String> value) {
        super.setValue(value);
        notifyObservers(value);
    }


}
