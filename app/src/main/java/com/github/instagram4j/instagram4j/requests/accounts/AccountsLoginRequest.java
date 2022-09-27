package com.github.instagram4j.instagram4j.requests.accounts;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.models.IGPayload;
import com.github.instagram4j.instagram4j.requests.IGPostRequest;
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse;
import com.github.instagram4j.instagram4j.utils.IGUtils;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountsLoginRequest extends IGPostRequest<LoginResponse> {
    @NonNull
    private String username;
    @NonNull
    private String password;

    @Override
    public String path() {
        return "accounts/login/";
    }

    @Override
    public IGPayload getPayload(IGClient client) {
        LoginPayload payload =  new LoginPayload(username, password);
        payload.device_id = client.getDeviceId();
        payload.guid = client.getGuid();
        payload.adid = IGUtils.randomUuid();
        payload.phone_id = client.getPhoneId();
        payload.jazoest = "22286";
        return payload;
    }

    @Override
    public Class<LoginResponse> getResponseType() {
        return LoginResponse.class;
    }

    @Data
    public static class LoginPayload extends IGPayload {
        @NonNull
        private String username;
        @NonNull
        private String enc_password;
        public String device_id;
        public String guid;
        public String adid;
        public String phone_id;
        public String jazoest;
        private int login_attempt_account = 0;
    }

}
