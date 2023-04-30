package com.healthcare.jsondto;

import lombok.Data;

@Data
public class ExtensionNewData {
    private String url;
    private String valueCode;
    private ValueCodeableConceptData valueCodeableConcept;
}
