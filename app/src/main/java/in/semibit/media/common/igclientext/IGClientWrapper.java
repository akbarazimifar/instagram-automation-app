package in.semibit.media.common.igclientext;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.IGClientActions;

import okhttp3.OkHttpClient;

public class IGClientWrapper extends IGClient {

    public String encryptionId, encryptionKey, authorization;
    public String sessionId;

    public IGClientWrapper(String username, String password) {
        super(username, password);
    }

    public IGClientWrapper(String username, String password, OkHttpClient client) {
        super(username, password, client);
    }

    public void setTransientValues(String encryptionId,String encryptionKey,String authorization,String sessionId){
        this.encryptionId = encryptionId;
        this.encryptionKey = encryptionKey;
        this.authorization = authorization;
        this.sessionId = sessionId;
    }

}
