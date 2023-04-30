package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FHIRExportDTO {
    @JsonProperty("request")
    private QueryDTO request;
    @JsonProperty("type")
    private String type; // "BATCH" or "TRNSC"
    @JsonProperty("body")
    private String body; //Bundle to POST (typically a search result)
}
