package com.healthcare.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PartnerProjectDTO {
	
	private Long id;
	private String description;
	private String email;
	private String mobileNumber;
	private String partnerName;
	private String primaryContactName;
	private List<ProjectResponseDTO> projects;
}
