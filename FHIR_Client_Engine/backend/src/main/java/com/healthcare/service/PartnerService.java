package com.healthcare.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.healthcare.dto.PartnerDTO;
import com.healthcare.dto.PartnerNamesDTO;
import com.healthcare.dto.PartnerProjectDTO;
import com.healthcare.dto.ProjectResponseDTO;
import com.healthcare.entity.PartnerDetails;
import com.healthcare.entity.ProjectDetails;
import com.healthcare.repository.PartnerRepo;
import com.healthcare.repository.ProjectRepo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PartnerService{

	@Autowired
	private PartnerRepo partnerRepo;
	
	@Autowired
	private ProjectRepo projectRepo;

	public List<PartnerProjectDTO> getAllPartner(Long id) {

		List<PartnerDetails> findAll = null;
		if (id != null) {
			findAll = partnerRepo.findPartnersById(id);
		} else {
			findAll = partnerRepo.findAll();
		}

		List<PartnerProjectDTO> partnerDTOList = new ArrayList<PartnerProjectDTO>();
		for (PartnerDetails partnerDetail : findAll) {
			PartnerProjectDTO dto = new PartnerProjectDTO();
			dto.setId(partnerDetail.getId());
			dto.setPartnerName(partnerDetail.getName());
			dto.setPrimaryContactName(partnerDetail.getPrimaryContactName());
			dto.setEmail(partnerDetail.getEmail());
			dto.setMobileNumber(partnerDetail.getPhone());
			dto.setDescription(partnerDetail.getDescription());
			List<ProjectDetails> findProjectsByPartnerId = projectRepo.getProjectsByPartnerId(partnerDetail.getId());
			List<ProjectResponseDTO> projectDTOs = new ArrayList<ProjectResponseDTO>();

			for (ProjectDetails projectDetails : findProjectsByPartnerId) {
				ProjectResponseDTO projectPartnerDTO = new ProjectResponseDTO();
				projectPartnerDTO.setId(projectDetails.getId());
				projectPartnerDTO.setProjectName(projectDetails.getProjectName());
				projectPartnerDTO.setStatus(projectDetails.getStatus());
				projectPartnerDTO.setFrequency(null);
				projectDTOs.add(projectPartnerDTO);
			}
			dto.setProjects(projectDTOs);
			partnerDTOList.add(dto);
		}
		log.debug("Fetched all partner successfully with the its projects");
		return partnerDTOList;
	}

	public PartnerDetails savePartner(PartnerDTO partnerDTO) {
		
		PartnerDetails partnerDetails = new PartnerDetails();
		partnerDetails.setName(partnerDTO.getPartnerName());
		partnerDetails.setDescription(partnerDTO.getDescription());
		partnerDetails.setEmail(partnerDTO.getEmail());
		partnerDetails.setPhone(partnerDTO.getMobileNumber());
		partnerDetails.setPrimaryContactName(partnerDTO.getPrimaryContactName());
		partnerDetails.setDeleted(false);
		partnerRepo.save(partnerDetails);
		log.debug("Created Partner successfully with the id: {}", partnerDetails.getId());

		return partnerDetails;
	}

	public List<PartnerNamesDTO> getAllNames() {

		List<Object[]> findAllNames = partnerRepo.findAllNames();
		List<PartnerNamesDTO> partnerNamesDTOs = new ArrayList<PartnerNamesDTO>();

		for (Object[] objects : findAllNames) {
			PartnerNamesDTO partnerNamesDTO = new PartnerNamesDTO();
			partnerNamesDTO.setId((long)objects[0]);
			partnerNamesDTO.setName((String) objects[1]);
			partnerNamesDTOs.add(partnerNamesDTO);
		}
		log.debug("Fetched all partner names successfully");
		return partnerNamesDTOs;
	}

	public PartnerDetails updatePartner(long partnerId, PartnerDTO partnerDTO) {

		Optional<PartnerDetails> partner = partnerRepo.findById(partnerId);
		if (partner.isPresent()) {
			PartnerDetails partnerDetails = partner.get();
			partnerDetails.setName(partnerDTO.getPartnerName());
			partnerDetails.setDescription(partnerDTO.getDescription());
			partnerDetails.setEmail(partnerDTO.getEmail());
			partnerDetails.setPhone(partnerDTO.getMobileNumber());
			partnerDetails.setPrimaryContactName(partnerDTO.getPrimaryContactName());
			log.debug("Updated {} partner successfully.", partnerDetails.getName());
			partnerRepo.save(partnerDetails);
			return partnerDetails;
		} else {
			log.error("Partner not found with Id: {}", partnerId);
			return null;
		}
	}

	public PartnerDetails deletePartner(Long id) {
		Optional<PartnerDetails> partnerOptional = partnerRepo.findById(id);
		if (partnerOptional.isPresent()) {
			PartnerDetails partnerDetails = partnerOptional.get();
			partnerDetails.setDeleted(true);

			Integer updateProjectByPartnerId = projectRepo.updateProjectByPartnerId(partnerDetails.getId());
			if (updateProjectByPartnerId != 0) {
				log.debug("Deleted projects for partner Id: {}", partnerDetails.getId());
			}

			log.debug("Deleted {} partner successfully.", partnerDetails.getName());
			return partnerRepo.save(partnerDetails);
		} else {
			log.error("Partner not deleted because partner not found with Id: {}", id);
			return null;
		}
	}
}
