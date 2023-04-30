package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class ResourceData {
    private String resourceType;
    private String id;
    private List<ExtensionData> extension;
    private List<IdentifierData> identifier;
    private Boolean active;
    private List<NameData> name;
    private List<TelecomData> telecom;
    private String gender;
    private String birthDate;
    private Boolean deceasedBoolean;
    private List<AddressData> address;
    private MaritalStatusData maritalStatus;
    private List<ContactData> contact;
}
