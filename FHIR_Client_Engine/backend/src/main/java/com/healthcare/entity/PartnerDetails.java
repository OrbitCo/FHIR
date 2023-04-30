package com.healthcare.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import lombok.Data;

@Entity
@Data
@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
@Table (name = "PARTNER_DETAILS")
public class PartnerDetails extends DefaultEntity {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name ="PRIMARY_CONTACT_NAME")
	private String primaryContactName;

	@OneToMany(fetch = FetchType.LAZY, mappedBy ="partnerDetails")
	private List<ProjectDetails> projectDetails;
}

