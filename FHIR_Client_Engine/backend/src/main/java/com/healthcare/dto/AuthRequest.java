package com.healthcare.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String authenticationType;
    private String grantType;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String clientCode;
    private String clientScope;
    private String redirectUrls;
}
