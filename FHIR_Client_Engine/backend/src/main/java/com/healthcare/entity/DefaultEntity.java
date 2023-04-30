package com.healthcare.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class DefaultEntity {
	
	@Column(name = "IS_DELETED")
	private boolean isDeleted ;

}
