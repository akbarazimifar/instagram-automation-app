package in.semibit.media.common.igclientext;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.requests.IGGetRequest;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;
import com.github.instagram4j.instagram4j.utils.IGUtils;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IGrequestHelper {

    public IGClient igClient;

    public IGrequestHelper(IGClient igClient) {
        this.igClient = igClient;
    }

    public String doIGGet(String path, Map<String, String> headers) {
        if (path.charAt(0) == '/')
            path = path.substring(1);
        String finalPath = path;
        IGGetRequest<StringIGResponse> igReq = new IGGetRequest<StringIGResponse>() {


            @Override
            public HttpUrl formUrl(IGClient client) {
                return HttpUrl.parse(baseApiUrl() + finalPath);
            }

            @Override
            protected Request.Builder applyHeaders(IGClient client, Request.Builder req) {
                Request.Builder orig = super.applyHeaders(client, req);
                if (headers != null) {
                    headers.keySet().forEach(key -> {
                        if (headers.get(key) != null)
                            req.addHeader(key, headers.get(key));
                    });
                }
                return orig;
            }

            @Override
            public StringIGResponse parseResponse(kotlin.Pair<Response, String> response) {
                StringIGResponse stringIGResponse = new StringIGResponse(response.getSecond());
                stringIGResponse.setStatusCode(response.getFirst().code());
                return stringIGResponse;
            }

            @Override
            public String path() {
                return finalPath;
            }

            @Override
            public Class<StringIGResponse> getResponseType() {
                return null;
            }
        };
        return igReq.execute(igClient).join().getBody();
    }

    public String doIGPost(String path, String payload, Map<String, String> headers) {
        return doIGPost(path, payload, true, headers);
    }

    public String doIGPost(String path, String payload, boolean isSignedBody, Map<String, String> headers) {
        if (path.charAt(0) == '/')
            path = path.substring(1);
        String finalPath = path;
        IGPostRequest<StringIGResponse> igReq = new IGPostRequest<StringIGResponse>() {


            @Override
            public HttpUrl formUrl(IGClient client) {
                return HttpUrl.parse(baseApiUrl() + finalPath);
            }

            @Override
            protected boolean isSigned() {
                return isSignedBody;
            }

            @Override
            protected Request.Builder applyHeaders(IGClient client, Request.Builder req) {
                Request.Builder orig = super.applyHeaders(client, req);
                if (headers != null) {
                    headers.keySet().forEach(key -> {
                        if (headers.get(key) != null) {
                            req.removeHeader(key);
                            req.addHeader(key, headers.get(key));
                        }

                    });
                }
                return orig;
            }

            @Override
            protected RequestBody getRequestBody(IGClient client) {

                if (isSigned()) {
                    return RequestBody.create(IGUtils.generateSignature(payload),
                            MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"));
                } else {
                    if (!payload.toLowerCase().startsWith("sig"))
                        return RequestBody.create(payload, MediaType.parse("application/json; charset=UTF-8"));
                    else
                        return RequestBody.create(payload, MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"));

                }
            }

            @Override
            protected IGPayload getPayload(IGClient client) {
                return null;
            }

            @Override
            public StringIGResponse parseResponse(kotlin.Pair<Response, String> response) {
                StringIGResponse stringIGResponse = new StringIGResponse(response.getSecond());
                stringIGResponse.setStatusCode(response.getFirst().code());
                return stringIGResponse;
            }

            @Override
            public String path() {
                return finalPath;
            }

            @Override
            public Class<StringIGResponse> getResponseType() {
                return StringIGResponse.class;
            }
        };
        return igReq.execute(igClient).join().getBody();
    }


}
