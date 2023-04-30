package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CSVExportDTO {
    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("columns")
    private List<String> columns = new ArrayList<>();

    @JsonProperty("selectedColumns")
    private List<String> selectedColumns = new ArrayList<>();

    @JsonProperty("body")
    private String body;

    @JsonProperty("emailExport")
    private EmailDTO email; //Null if email is not specified

    @JsonProperty("SFTPExport")
    private SFTPDTO sftp; //Null if SFTP is not specified
}
