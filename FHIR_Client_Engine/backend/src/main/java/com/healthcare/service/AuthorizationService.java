package com.healthcare.service;

import com.healthcare.dto.AuthRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import security.CreateJWT;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthorizationService {

    private String accessToken;

    public String fetchToken(AuthRequest authRequest) throws IOException {
        if(authRequest.getGrantType().equals("client_jwt")) {
            /**
             * @see https://git.bcbssc.com/InformationSystems/fhir-java-utilities
             */
            String token = null;
            try {
                token = CreateJWT.generateEncodedToken(authRequest.getClientId(), authRequest.getTokenEndpoint(),
                        authRequest.getClientSecret());
            } catch (security.FhirJWTException e) { 
                log.debug(e.getMessage());
                JSONObject ErrorResponseBody = new JSONObject();
                ErrorResponseBody.put("error", "Authorization denied.");
                return ErrorResponseBody.toString();
            }
            System.out.println(token);
            authRequest.setClientSecret(token);
        }
        OkHttpClient client = buildHttpClient();
        Request request = buildPOSTRequest(authRequest);
        Response response = client.newCall(request).execute();
        String responseBody = response.body() != null ? response.body().string() : null;
        if (response.code() != 200) {
            log.debug(response.message());
            JSONObject ErrorResponseBody = new JSONObject();
            ErrorResponseBody.put("error", "Authorization denied.");
            return ErrorResponseBody.toString();
        } else {
            extractToken(responseBody);
        }
        return responseBody;
    }


    OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request buildPOSTRequest(AuthRequest authRequest) {
        return new Request.Builder()
                .url(authRequest.getTokenEndpoint())
                .post(getFormBody(authRequest))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }

    private RequestBody getFormBody(AuthRequest authRequest) {
        FormBody.Builder myBuilder = new FormBody.Builder();

        if(authRequest.getGrantType().equals("client_jwt")) {
            myBuilder.add("grant_type", "client_credentials")
                    .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                    .add("client_assertion", authRequest.getClientSecret());
        } else {
            myBuilder.add("grant_type", authRequest.getGrantType())
                    .add("client_id", authRequest.getClientId())
                    .add("client_secret", authRequest.getClientSecret());
        }
        if(authRequest.getGrantType().equals("authorization_code")) {
            myBuilder.add("code", authRequest.getClientCode())
                    .add("redirect_uri", authRequest.getRedirectUrls())
                    .add("scope", authRequest.getClientScope());
        }
        return myBuilder.build();
    }

    private void extractToken(String token) {
        JSONObject tokenInfo = new JSONObject(token);
        log.debug("Token received: {}", tokenInfo);
        if (tokenInfo.has("access_token")) {
            accessToken = (String) tokenInfo.get("access_token");
        }
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void flushAccessToken() {
        this.accessToken = "";
    }

}
