package com.healthcare.entity;

import javax.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;

import java.util.List;

@Entity
@Data
@Table(name = "PROJECT_DETAILS")
@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
@SQLDelete(sql = "update PROJECT_DETAILS set is_deleted = true where id = ?")
public class ProjectDetails extends DefaultEntity{

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME")
	private String projectName;
	
	@Column(name = "DESCRIPTION")
	private String projectDescription;

	@Column(name = "EMAIL")
	private String contactEmail;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "URI")
	private String uri;

	@Column(name = "port")
	private Integer port;

	@Column(name = "AUTHENTICATIONS")
	@Lob
	private byte[] authentications;

	//TODO: Remove below in favor of Authentications
	@Column(name = "AUTHENTICATION")
	private String authentication;

	@Column(name = "GRANT_TYPE")
	private String grantType;

	@Column(name = "AUTHENTICATION_ENDPOINT")
	private String authenticationEndpoint;

	@Column(name = "TOKEN_ENDPOINT")
	private String tokenEndpoint;
	
	@Column(name = "CLIENT_ID")
	private String clientId;

	@Column(name = "CLIENT_SECREATE")
	private String clientScreate;

	@Column(name = "REDIRECT_URL")
	private String redirectUrl;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "QUERY")
	private String query;
	
	@Column(name = "CONNECTION")
	private String connection;

	@Column(name = "OUTPUT")
	private String output;

	@JoinColumn(name = "PARTNER_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private PartnerDetails partnerDetails;

	@OneToMany(fetch = FetchType.LAZY, mappedBy ="projectDetails", cascade = CascadeType.ALL)
	private List<ProjectOutput> projectOutput;

	@Column(name = "OUTPUT_JSON")
	@Lob
	private byte[] outputJson;

}
