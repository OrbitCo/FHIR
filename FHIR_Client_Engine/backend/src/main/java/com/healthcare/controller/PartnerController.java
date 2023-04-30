package com.healthcare.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.healthcare.dto.PartnerDTO;
import com.healthcare.dto.PartnerNamesDTO;
import com.healthcare.dto.PartnerProjectDTO;
import com.healthcare.entity.PartnerDetails;
import com.healthcare.service.PartnerService;

@RestController
@RequestMapping(value = "/api/partner", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PartnerController {

	@Autowired
	PartnerService partnerService;

	@PostMapping(value = "/create-partner", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PartnerNamesDTO> createPartner(@RequestBody PartnerDTO partnerDTO) {
		PartnerDetails savePartner = partnerService.savePartner(partnerDTO);
		PartnerNamesDTO dto = new PartnerNamesDTO();
		dto.setId(savePartner.getId());
		dto.setName(savePartner.getName());
		return new ResponseEntity<PartnerNamesDTO>(dto, HttpStatus.OK);
	}

	@GetMapping(value = "/get-all-partner", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PartnerProjectDTO>> getAllPartner(@RequestParam (name = "partnerId", required = false) Long id) {
		return new ResponseEntity<List<PartnerProjectDTO>>(partnerService.getAllPartner(id), HttpStatus.OK);
	}

	@GetMapping(value = "/get-partner-names", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PartnerNamesDTO>> getAllNames() {
		return new ResponseEntity<List<PartnerNamesDTO>>(partnerService.getAllNames(), HttpStatus.OK);
	}

	@PutMapping(value = "/update-partner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updatePartner(@PathVariable("id") long id, @RequestBody PartnerDTO partnerDTO) {
		PartnerDetails updatePartner = partnerService.updatePartner(id, partnerDTO);
		if (updatePartner != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/delete-partner/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deletePartner(@PathVariable("id") Long id) {
		PartnerDetails deletePartner = partnerService.deletePartner(id);
		if (deletePartner != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
