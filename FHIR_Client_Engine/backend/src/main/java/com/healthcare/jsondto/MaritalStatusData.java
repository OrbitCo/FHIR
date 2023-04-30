package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class MaritalStatusData {
    private List<CodingData> coding;
}
