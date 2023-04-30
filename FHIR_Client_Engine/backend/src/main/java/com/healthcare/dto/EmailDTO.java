package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmailDTO {
    @JsonProperty("to")
    private String to;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;
}
