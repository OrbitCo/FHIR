package com.healthcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.entity.ProjectDetails;

public interface ProjectRepo extends JpaRepository<ProjectDetails, Long>{

	@Query("from ProjectDetails where partnerDetails.id = :partnerId and is_deleted = false")
	public List<ProjectDetails> getProjectsByPartnerId(@Param("partnerId") Long partnerId);
	
	@Transactional
	@Modifying
	@Query("update ProjectDetails set is_deleted = true where partnerDetails.id = :partnerId")
	public Integer updateProjectByPartnerId(@Param("partnerId") Long partnerId);

}
