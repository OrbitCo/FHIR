package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class IdentifierTypeData {
    private List<CodingData> coding;
    private String text;
}
