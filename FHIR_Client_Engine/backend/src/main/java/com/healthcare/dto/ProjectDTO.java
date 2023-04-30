package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.healthcare.entity.ProjectOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ProjectDTO {

	private Long id;
	private String projectName;
	private String description;
	private String email;
	private String contactNumber;
	private String uri;
	private int port;
	private String basicResponseUri;
	private Integer basicResponsePort;
	private String authentication;
	private String grantType;
	private String authorizationEndpoint;
	private String tokenEndpoint;
	private String clientId;
	private String clientSecret;
	private String redirectUrls;
	private Long partnerId;
	private String partnerName;
	@JsonProperty("isDeleted")
	private boolean isDeleted;
	@JsonProperty("isPartnerDeleted")
	private boolean isPartnerDeleted;
	private String status;
	private String query;
	private String connection;
	private String frequency;
	private List<ProjectOutPutDTO> projectOutputList;
	private String authentications;
	private String outputJson;
}
