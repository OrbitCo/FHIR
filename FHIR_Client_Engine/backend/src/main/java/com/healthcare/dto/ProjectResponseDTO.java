package com.healthcare.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProjectResponseDTO {

	private Long id;
	private String projectName;
	private String status;
	private String frequency;
}
