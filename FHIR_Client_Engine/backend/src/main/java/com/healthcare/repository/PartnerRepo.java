package com.healthcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcare.entity.PartnerDetails;

public interface PartnerRepo extends JpaRepository<PartnerDetails, Long>{

	@Query("select id, name from PartnerDetails where is_deleted = false order by name asc")
	public List<Object[]> findAllNames();

	@Query("from PartnerDetails where is_deleted = false and id= :id")
	public List<PartnerDetails> findPartnersById(@Param("id")Long id);

}
