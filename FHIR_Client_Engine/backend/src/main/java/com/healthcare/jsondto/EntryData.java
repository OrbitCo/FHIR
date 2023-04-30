package com.healthcare.jsondto;

import lombok.Data;

@Data
public class EntryData {
    private String fullUrl;
    ResourceData resource;
    RequestData request;
}
