package in.semibit.media.postbot.poc;

import com.github.instagram4j.instagram4j.responses.IGResponse;

public class StringIGResponse extends IGResponse {
    String body;

    public StringIGResponse(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
