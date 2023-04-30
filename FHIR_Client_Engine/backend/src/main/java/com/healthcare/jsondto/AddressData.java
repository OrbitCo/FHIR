package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class AddressData {
    private String use;
    private String type;
    private String text;
    private List<String> line;
    private String country;
    private String city;
    private String district;
    private String state;
    private String postalCode;
    private PeriodData period;
}
