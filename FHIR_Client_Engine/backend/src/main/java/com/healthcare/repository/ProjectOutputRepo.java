package com.healthcare.repository;

import com.healthcare.entity.ProjectOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectOutputRepo extends JpaRepository<ProjectOutput, Long> {

}
