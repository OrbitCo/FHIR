package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectOutPutDTO {

    private Long id;
    private String outputName;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    private Long projectId;
    private String projectName;
    private String outputSettings;

}
