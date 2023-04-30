package com.healthcare.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PartnerDTO {

	private Long id;
	private String description;
	private String email;
	private String mobileNumber;
	private String partnerName;
	private String primaryContactName;
}
