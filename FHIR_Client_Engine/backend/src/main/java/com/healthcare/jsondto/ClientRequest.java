package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class ClientRequest {
    private String resourceType;
    private String type;
    private List<EntryData> entry;
}
