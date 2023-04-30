package com.healthcare.jsondto;

import lombok.Data;

import java.util.List;

@Data
public class ResourceNewData {
    private String resourceType;
    private String id;
    private List<ExtensionNewData> extension;
    private List<IdentifierData> identifier;
    private boolean active;
    private List<NameNewData> name;
    private List<TelecomData> telecom;
    private String gender;
    private String birthDate;
    private boolean deceasedBoolean;
    private List<AddressData> address;
    private MaritalStatusData maritalStatus;
    private List<ContactData> contact;
}
