package com.healthcare.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.healthcare.dto.APIResponse;
import com.healthcare.dto.ProjectOutPutDTO;
import com.healthcare.entity.ProjectOutput;
import com.healthcare.repository.ProjectOutputRepo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.healthcare.dto.ProjectDTO;
import com.healthcare.entity.PartnerDetails;
import com.healthcare.entity.ProjectDetails;
import com.healthcare.exception.NoSuchProjectExistsException;
import com.healthcare.repository.PartnerRepo;
import com.healthcare.repository.ProjectRepo;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ProjectService {

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private PartnerRepo partnerRepo;

    @Autowired
    private ProjectOutputRepo projectOutputRepo;
    private final IParser FHIR_PARSER = FhirContext.forR4().newJsonParser();

	public List<ProjectDTO> getAllProject() {
		List<ProjectDetails> findAll = projectRepo.findAll();
		List<ProjectDTO> projectDtoList = new ArrayList<ProjectDTO>();
		for (ProjectDetails projectDetail : findAll) {
            projectDtoList.add(getProjectById(projectDetail.getId()));
        }
        log.debug("Fetched all projects successfully");
        return projectDtoList;
    }

    private List<ProjectOutPutDTO> toProjectOutPutDTOS(List<ProjectOutput> projectOutputs) {
        List<ProjectOutPutDTO> projectOutPutDTOS = new ArrayList<>();
        for (ProjectOutput projectOutput : projectOutputs) {
            ProjectOutPutDTO projectOutPutDTO = new ProjectOutPutDTO();
            projectOutPutDTO.setProjectId(projectOutput.getProjectDetails().getId());
            projectOutPutDTO.setId(projectOutput.getId());
            projectOutPutDTO.setProjectName(projectOutput.getProjectDetails().getProjectName());
            projectOutPutDTO.setOutputName(projectOutput.getOutputName());
            if(projectOutput.getOutputSettings() != null) {
                projectOutPutDTO.setOutputSettings(new String(projectOutput.getOutputSettings()));
            }
            projectOutPutDTOS.add(projectOutPutDTO);
        }
        return projectOutPutDTOS;
    }

    public ProjectDetails saveProject(ProjectDTO projectDTO) {
        ProjectDetails projectDetails = new ProjectDetails();
        projectDetails.setProjectName(projectDTO.getProjectName());
        projectDetails.setProjectDescription(projectDTO.getDescription());
        projectDetails.setContactEmail(projectDTO.getEmail());
        projectDetails.setPhone(projectDTO.getContactNumber());
        projectDetails.setUri(projectDTO.getUri());
        projectDetails.setPort(projectDTO.getPort());
        projectDetails.setAuthentication(projectDTO.getAuthentication());
        projectDetails.setGrantType(projectDTO.getGrantType());
        projectDetails.setAuthenticationEndpoint(projectDTO.getAuthorizationEndpoint());
        projectDetails.setTokenEndpoint(projectDTO.getTokenEndpoint());
        projectDetails.setClientId(projectDTO.getClientId());
        projectDetails.setClientScreate(projectDTO.getClientSecret());
        projectDetails.setRedirectUrl(projectDTO.getRedirectUrls());
        projectDetails.setDeleted(false);
        projectDetails.setQuery(projectDTO.getQuery());
        projectDetails.setConnection(projectDTO.getConnection());
        projectDetails.setStatus(projectDTO.getStatus());

        Optional<PartnerDetails> partnerDetails = partnerRepo.findById(projectDTO.getPartnerId());
        if (partnerDetails.isPresent()) {
            projectDetails.setPartnerDetails(partnerDetails.get());
        }
        log.debug("Created project successfully with the name: {}", projectDetails.getProjectName());
        projectDetails = projectRepo.save(projectDetails);
        if (projectDTO.getProjectOutputList() != null) {
            toProjectOutputList(projectDTO.getProjectOutputList(), projectDetails);
        }
        return projectDetails;
    }

    private List<ProjectOutput> toProjectOutputList(List<ProjectOutPutDTO> projectOutPutDTOS, ProjectDetails projectDetails) {
        List<ProjectOutput> projectOutputList = new ArrayList<>();
        List<ProjectOutput> projectOutputs = projectDetails.getProjectOutput();
        if (projectOutputs != null) {
            for (ProjectOutput projectOutput : projectOutputs) {
                boolean exists = false;
                for (ProjectOutPutDTO projectOutputDTO : projectOutPutDTOS) {
                    if (projectOutputDTO.getOutputName().equals(projectOutput.getOutputName())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    projectOutputRepo.delete(projectOutput);
                }
            }
        }
        for (ProjectOutPutDTO projectOutput : projectOutPutDTOS) {
            ProjectOutput output = new ProjectOutput();
            if (projectOutput.getId() != null) {
                Optional<ProjectOutput> outputs = projectOutputRepo.findById(projectOutput.getId());
                if (outputs.isPresent()) {
                    output = outputs.get();
                }
            }
            output.setOutputName(projectOutput.getOutputName());
            output.setProjectDetails(projectDetails);
            output.setOutputSettings(projectOutput.getOutputSettings().getBytes());
            projectOutputRepo.save(output);

            projectOutputList.add(output);
        }
        return projectOutputList;
    }

    public ProjectDetails updateProject(Long projectId, ProjectDTO projectDTO) {
        Optional<ProjectDetails> project = projectRepo.findById(projectId);
        if (project.isPresent()) {
            ProjectDetails projectDetails = project.get();
            projectDetails.setProjectName(projectDTO.getProjectName());
            projectDetails.setProjectDescription(projectDTO.getDescription());
            projectDetails.setContactEmail(projectDTO.getEmail());
            projectDetails.setPhone(projectDTO.getContactNumber());
            projectDetails.setUri(projectDTO.getUri());
            projectDetails.setPort(projectDTO.getPort());
            projectDetails.setAuthentication(projectDTO.getAuthentication());
            projectDetails.setGrantType(projectDTO.getGrantType());
            projectDetails.setAuthenticationEndpoint(projectDTO.getAuthorizationEndpoint());
            projectDetails.setTokenEndpoint(projectDTO.getTokenEndpoint());
            projectDetails.setClientId(projectDTO.getClientId());
            projectDetails.setClientScreate(projectDTO.getClientSecret());
            projectDetails.setRedirectUrl(projectDTO.getRedirectUrls());
            projectDetails.setStatus(projectDTO.getStatus());
            projectDetails.setQuery(projectDTO.getQuery());
            projectDetails.setConnection(projectDTO.getConnection());
            projectDetails.setAuthentications(projectDTO.getAuthentications().getBytes());
            projectDetails.setOutputJson(projectDTO.getOutputJson().getBytes());
            //projectDetails.setDeleted(false);

            Optional<PartnerDetails> partnerOptional = partnerRepo.findById(projectDTO.getPartnerId());
            if (partnerOptional.isPresent()) {
                projectDetails.setPartnerDetails(partnerOptional.get());
            } else {
                log.error("Partner not found with Id : {}", projectDTO.getPartnerId());
            }

            projectRepo.save(projectDetails);
            projectDetails = projectRepo.save(projectDetails);
            if (projectDTO.getProjectOutputList() != null) {
                toProjectOutputList(projectDTO.getProjectOutputList(), projectDetails);
            }
            log.debug("Updated {} project successfully.", projectDetails.getProjectName());
            return projectDetails;
        } else {
            log.error("Project not found with Id: {}", projectId);
            return null;
        }
    }

    public ProjectDetails deleteProject(Long id) {
        Optional<ProjectDetails> projectOptional = projectRepo.findById(id);
        if (projectOptional.isPresent()) {
            ProjectDetails partnerDetails = projectOptional.get();
            partnerDetails.setDeleted(true);
            log.debug("Deleted project id {} successfully.", id);
            return projectRepo.save(partnerDetails);
        } else {
            log.error("Project not found with Id: {}", id);
            return null;
        }
    }

    public ProjectDTO getProjectById(Long id) {
        Optional<ProjectDetails> projectOptional = projectRepo.findById(id);
        if (projectOptional.isPresent()) {
            ProjectDetails projectDetail = projectOptional.get();
            ProjectDTO dto = new ProjectDTO();
            dto.setId(projectDetail.getId());
            dto.setProjectName(projectDetail.getProjectName());
            dto.setDescription(projectDetail.getProjectDescription());
            dto.setEmail(projectDetail.getContactEmail());
            dto.setContactNumber(projectDetail.getPhone());
            if(projectDetail.getAuthentications() != null) {
                dto.setAuthentications(new String(projectDetail.getAuthentications()));
            }
            if(projectDetail.getOutputJson() != null) {
                dto.setOutputJson(new String(projectDetail.getOutputJson()));
            }
            dto.setUri(projectDetail.getUri());
            dto.setPort(projectDetail.getPort());
            dto.setAuthentication(projectDetail.getAuthentication());
            dto.setGrantType(projectDetail.getGrantType());
            dto.setAuthorizationEndpoint(projectDetail.getAuthenticationEndpoint());
            dto.setTokenEndpoint(projectDetail.getTokenEndpoint());
            dto.setClientId(projectDetail.getClientId());
            dto.setClientSecret(projectDetail.getClientScreate());
            dto.setRedirectUrls(projectDetail.getRedirectUrl());
            dto.setDeleted(projectDetail.isDeleted());
            dto.setStatus(projectDetail.getStatus());
            dto.setQuery(projectDetail.getQuery());
            dto.setConnection(projectDetail.getConnection());
            dto.setFrequency(null);

            if (projectDetail.getPartnerDetails() != null) {
                dto.setPartnerId(projectDetail.getPartnerDetails().getId());
                dto.setPartnerName(projectDetail.getPartnerDetails().getName());
                dto.setPartnerDeleted(projectDetail.getPartnerDetails().isDeleted());
            }
            if (projectDetail.getProjectOutput() != null) {
                dto.setProjectOutputList(toProjectOutPutDTOS(projectDetail.getProjectOutput()));
            }
            log.debug("Fetched project: {} successfully.", projectDetail.getProjectName());
            return dto;
        } else {
            log.error("Project not found with Id: {}", id);
            throw new NoSuchProjectExistsException("No Such Project exists with id: " + id);
        }
    }
}