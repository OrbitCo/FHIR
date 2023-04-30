package com.healthcare.jsondto;

import lombok.Data;

@Data
public class IdentifierData {
    private String use;
    private IdentifierTypeData type;
    private String system;
    private String value;
    private PeriodData period;
    private IdentifierAssignerData assigner;
}
