package com.healthcare.jsondto;

import lombok.Data;

@Data
public class EntryNewData {
    private String fullUrl;
    ResourceNewData resource;
    RequestData request;
}
