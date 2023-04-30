package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class NameData {
    private String use;
    private String family;
    private List<String> given;
    private String text;
}
