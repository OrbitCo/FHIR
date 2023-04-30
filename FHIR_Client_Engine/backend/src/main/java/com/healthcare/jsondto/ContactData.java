package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class ContactData {
    private List<RelationshipData> relationship;
    private NameData name;
    private TelecomData telecom;
    private AddressData address;
    private String gender;
    private PeriodData period;
}
