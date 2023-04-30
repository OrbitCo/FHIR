package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class NameNewData {
    private String use;
    private List<String> family;
    private List<String> given;
    private String text;
}
