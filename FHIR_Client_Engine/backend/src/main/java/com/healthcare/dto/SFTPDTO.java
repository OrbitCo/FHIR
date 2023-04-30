package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SFTPDTO {
    @JsonProperty("serverAddress")
    private String serverAddress;

    @JsonProperty("serverPort")
    private String serverPort;

    @JsonProperty("targetDirectory")
    private String targetDirectory;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("privateKeyBase64")
    private String privateKeyBase64;
}
