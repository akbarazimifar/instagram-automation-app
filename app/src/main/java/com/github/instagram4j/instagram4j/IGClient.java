package com.github.instagram4j.instagram4j;

import com.github.instagram4j.instagram4j.actions.IGClientActions;
import com.github.instagram4j.instagram4j.exceptions.ExceptionallyHandler;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException.IGFailedResponse;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.IGRequest;
import com.github.instagram4j.instagram4j.requests.accounts.AccountsLoginRequest;
import com.github.instagram4j.instagram4j.requests.accounts.AccountsTwoFactorLoginRequest;
import com.github.instagram4j.instagram4j.requests.qe.QeSyncRequest;
import com.github.instagram4j.instagram4j.responses.IGResponse;
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import com.github.instagram4j.instagram4j.utils.SerializableCookieJar;
import com.github.instagram4j.instagram4j.utils.SerializeUtil;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import kotlin.Pair;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Data
@Slf4j
public class IGClient implements Serializable {
    public static final long serialVersionUID = -893265874836l;
    public final String $username;
    public final String $password;
    public String encryptionId, encryptionKey, authorization;
    @Accessors(chain = true)
    public transient OkHttpClient httpClient;
    public String sessionId;
    public transient IGClientActions actions;
    @Accessors(chain = true)
    public transient ExceptionallyHandler exceptionallyHandler;
    public String deviceId;
    public String guid;
    public String phoneId;
    @Setter(AccessLevel.PRIVATE)
    public boolean loggedIn = false;
    @Setter(AccessLevel.PRIVATE)
    public Profile selfProfile;
    @Accessors(chain = true)
    public IGDevice device = IGAndroidDevice.GOOD_DEVICES[6];

    public IGClient(String username, String password) {
        this(username, password, IGUtils.defaultHttpClientBuilder().build());
    }

    public IGClient(String username, String password, OkHttpClient client) {
        this.$username = username;
        this.$password = password;
        this.guid = IGUtils.randomUuid();
        this.phoneId = IGUtils.randomUuid();
        this.deviceId = IGUtils.generateDeviceId(username, password);
        this.httpClient = client;
        this.initializeDefaults();
    }

    public void initializeDefaults() {
        this.sessionId = IGUtils.randomUuid();
        this.actions = new IGClientActions(this);
        this.exceptionallyHandler = new ExceptionallyHandler() {

            @Override
            public <T> T handle(Throwable throwable, Class<T> type) {
                throw new CompletionException(throwable.getCause());
            }

        };
    }

    public IGClientActions actions() {
        return this.actions;
    }

    public CompletableFuture<LoginResponse> sendLoginRequest() {
        return new QeSyncRequest().execute(this)
                .thenCompose(res -> {
                    IGUtils.sleepSeconds(1);
                    return new AccountsLoginRequest($username,
                            IGUtils.encryptPassword(this.$password, this.encryptionId,
                                    this.encryptionKey)).execute(this);
                })
                .thenApply((res) -> {
                    this.setLoggedInState(res);

                    return res;
                });
    }

    public CompletableFuture<LoginResponse> sendLoginRequest(String code, String identifier) {
        return new QeSyncRequest().execute(this)
                .thenCompose(res -> new AccountsTwoFactorLoginRequest($username,
                        IGUtils.encryptPassword(this.$password, this.encryptionId,
                                this.encryptionKey),
                        code,
                        identifier).execute(this))
                .thenApply((res) -> {
                    this.setLoggedInState(res);

                    return res;
                });
    }

    public <T extends IGResponse> CompletableFuture<T> sendRequest(@NonNull IGRequest<T> req) {
        CompletableFuture<Pair<Response, String>> responseFuture = new CompletableFuture<>();
        log.info("Sending request : {}", req.formUrl(this).toString());
        this.httpClient.newCall(req.formRequest(this)).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException exception) {
                responseFuture.completeExceptionally(exception);
            }

            @Override
            public void onResponse(Call call, Response res) throws IOException {
                log.info("Response for {} : {}", call.request().url().toString(), res.code());
                try (ResponseBody body = res.body()) {
                    responseFuture.complete(new Pair<>(res, body.string()));
                }
            }

        });

        return responseFuture
                .thenApply(res -> {
                    setFromResponseHeaders(res.getFirst());
                    log.info("Response for {} with body (truncated) : {}",
                            res.getFirst().request().url(),
                            IGUtils.truncate(res.getSecond()));

                    return req.parseResponse(res);
                })
                .exceptionally((tr) -> {
                    return this.exceptionallyHandler.handle(tr, req.getResponseType());
                });
    }

    public void setLoggedInState(LoginResponse state) {
        if (!state.getStatus().equals("ok"))
            return;
        this.loggedIn = true;
        this.selfProfile = state.getLogged_in_user();
        log.info("Logged into {} ({})", selfProfile.getUsername(), selfProfile.getPk());
    }

    public String getCsrfToken() {
        return IGUtils.getCookieValue(this.getHttpClient().cookieJar(), "csrftoken")
                .orElse("missing");
    }


    public Map<String, String> getDynamicHeaders() {
        if (dynamicHeaders == null)
            dynamicHeaders = new HashMap<>();
        return dynamicHeaders;
    }

    public Map<String, String> dynamicHeaders = new HashMap<>();

    public void setFromResponseHeaders(Response res) {
        Optional.ofNullable(res.header("ig-set-password-encryption-key-id"))
                .ifPresent(s -> this.encryptionId = s);
        Optional.ofNullable(res.header("ig-set-password-encryption-pub-key"))
                .ifPresent(s -> this.encryptionKey = s);
        if (res.header("ig-set-authorization") != null && res.header("ig-set-authorization").length() > 15) {
            this.authorization = res.header("ig-set-authorization");
            dynamicHeaders.put("authorization", this.authorization);
        }
        for (Iterator<Pair<String, String>> it = res.headers().iterator(); it.hasNext(); ) {
            Pair<String, String> header = it.next();
            if (header.getFirst().toLowerCase().startsWith("ig-set-ig") || header.getFirst().toLowerCase().startsWith("ig-set-x"))
                getDynamicHeaders().put(header.getFirst().toLowerCase().replace("ig-set-", ""), header.getSecond());
            if (header.getFirst().toLowerCase().contains("www-claim"))
                getDynamicHeaders().put("x-ig-www-claim".toLowerCase(), header.getSecond());
        }
    }

    public IGPayload setIGPayloadDefaults(IGPayload load) {
        load.set_csrftoken(this.getCsrfToken());
        load.setDevice_id(this.deviceId);
        if (selfProfile != null) {
            load.set_uid(selfProfile.getPk().toString());
            load.set_uuid(this.guid);
        } else {
            load.setId(this.guid);
        }
        load.setGuid(this.guid);
        load.setPhone_id(this.phoneId);

        return load;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static IGClient deserialize(File clientFile, File cookieFile)
            throws ClassNotFoundException, IOException {
        return deserialize(clientFile, cookieFile, IGUtils.defaultHttpClientBuilder());
    }

    public static IGClient deserialize(File clientFile, File cookieFile,
                                       OkHttpClient.Builder clientBuilder) throws ClassNotFoundException, IOException {
        IGClient client = SerializeUtil.deserialize(clientFile, IGClient.class);
        CookieJar jar = SerializeUtil.deserialize(cookieFile, SerializableCookieJar.class);

        client.httpClient = clientBuilder
                .cookieJar(jar)
                .build();

        return client;
    }

    public void serialize(File clientFile, File cookieFile) throws IOException {
        SerializeUtil.serialize(this, clientFile);
        SerializeUtil.serialize(this.httpClient.cookieJar(), cookieFile);
    }

    public Object readResolve() throws ObjectStreamException {
        this.initializeDefaults();
        if (loggedIn)
            log.info("Logged into {} ({})", selfProfile.getUsername(), selfProfile.getPk());
        return this;
    }

    private transient String pigeonSessionId;

    public String currentPegionSession() {
        if (pigeonSessionId == null)
            pigeonSessionId = "UFS-" + IGUtils.randomUuid();
        return pigeonSessionId;
    }

    @Accessors(fluent = true)
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Builder {
        public String username;
        public String password;
        public OkHttpClient client;
        public IGDevice device = IGAndroidDevice.GOOD_DEVICES[6];
        public LoginHandler onChallenge;
        public LoginHandler onTwoFactor;
        public BiConsumer<IGClient, LoginResponse> onLogin = (client, login) -> {
        };

        public IGClient build() {
            return new IGClient(username, password, Optional.ofNullable(client)
                    .orElseGet(() -> IGUtils.defaultHttpClientBuilder().build())).setDevice(device);
        }

        public IGClient simulatedLogin(Consumer<List<CompletableFuture<?>>> postLoginResponses)
                throws IGLoginException {
            IGClient client = build();
            client.actions.simulate().preLoginFlow().forEach(CompletableFuture::join);
            onLogin.accept(client, performLogin(client));
            postLoginResponses.accept(client.actions.simulate().postLoginFlow());

            return client;
        }

        public IGClient simulatedLogin() throws IGLoginException {
            return simulatedLogin((res) -> {
            });
        }

        public IGClient login() throws IGLoginException {
            IGClient client = build();

            onLogin.accept(client, performLogin(client));

            return client;
        }

        public LoginResponse performLogin(IGClient client) throws IGLoginException {
            LoginResponse response = client.sendLoginRequest()
                    .exceptionally(tr -> {
                        LoginResponse loginResponse =
                                IGFailedResponse.of(tr.getCause(), LoginResponse.class);
                        if (loginResponse.getTwo_factor_info() != null && onTwoFactor != null) {
                            loginResponse = onTwoFactor.accept(client, loginResponse);
                        }
                        if (loginResponse.getChallenge() != null && onChallenge != null) {
                            loginResponse = onChallenge.accept(client, loginResponse);
                            client.setLoggedInState(loginResponse);
                        }

                        return loginResponse;
                    })
                    .join();

            if (!client.isLoggedIn()) {
                throw new IGLoginException(client, response);
            }

            return response;
        }

        @FunctionalInterface
        public static interface LoginHandler {
            public LoginResponse accept(IGClient client, LoginResponse t);
        }

    }
}
