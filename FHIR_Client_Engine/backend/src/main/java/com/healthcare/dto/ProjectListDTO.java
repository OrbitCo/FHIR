package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProjectListDTO {

	private Long id;
	private String projectName;
	private String description;
	@JsonProperty("isDeleted")
	private boolean isDeleted;
	private String partnerName;
}
