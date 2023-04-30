package com.healthcare.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import com.healthcare.dto.APIResponse;
import com.healthcare.dto.CSVExportDTO;
import com.healthcare.dto.FHIRExportDTO;
import com.healthcare.service.*;
import lombok.extern.slf4j.Slf4j;
import com.healthcare.dto.ProjectDTO;
import com.healthcare.entity.ProjectDetails;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping(value = "/api/project", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProjectController {

	@Autowired
	private CSVService csvService;

	@Autowired
	private SFTPService sftpService;

	@Autowired
	private EmailService emailService;

	@Autowired
	ProjectService projectService;

	@Autowired
	ExportService exportService;

	@PostMapping(value = "/create-project", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createProject(@RequestBody ProjectDTO projectDTO) {
		projectService.saveProject(projectDTO);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/get-all-project", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ProjectDTO>> getAllProject() {
		return new ResponseEntity<List<ProjectDTO>>(projectService.getAllProject(), HttpStatus.OK);
	}

	@PutMapping(value = "/update-project/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateProject(@PathVariable("id") Long id, @RequestBody ProjectDTO projectDTO) {
		ProjectDetails updateProject = projectService.updateProject(id, projectDTO);
		if (updateProject != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping(value = "/delete-project/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> deleteProject(@PathVariable("id") Long id) {
		ProjectDetails deleteProject = projectService.deleteProject(id);
		if (deleteProject != null) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/get-project/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProjectDTO> getProjectById(@PathVariable("id") Long id) {
		ProjectDTO projectById = projectService.getProjectById(id);
			return new ResponseEntity<ProjectDTO>(projectById, HttpStatus.OK);
	}

	@PostMapping(value = "/import-data-to-client",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public APIResponse importDataToClient(@RequestBody FHIRExportDTO fhirExportDTO) throws IOException {
		return exportService.importDataToClient(fhirExportDTO);
	}

	@PostMapping(value = "/download-csv")
	public void downloadCSV(@RequestBody CSVExportDTO data, HttpServletResponse response) {
		try {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; file=" + data.getFileName());
			csvService.downloadPatientCSV(response.getWriter(), data.getSelectedColumns(), data.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error downloading patient csv: {}", e.getMessage());
		}
	}

	@PostMapping(value = "/get-fhir-paths-from-json")
	public List<String> getAllFhirPathsFromJSON(@RequestBody String json) {
		try {
			return csvService.getAllPathsFromJSON(json);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error fetching paths from json: {}", e.getMessage());
		}
		return null;
	}

	@PostMapping(value = "/download-patient-csv-basic-auth")
	public void downloadPatientCSVBasicAuth(@RequestBody CSVExportDTO data, HttpServletResponse response) {
		try {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; file=" + data.getFileName());
			csvService.downloadPatientCSVBasicAuth(response.getWriter(), data.getColumns(), data.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error downloading patient csv: {}", e.getMessage());
		}
	}

	@PostMapping(value = "/upload-csv-to-sftp")
	public ResponseEntity<Object> uploadCSVToSFTP(@RequestBody CSVExportDTO data) {
		try {
			InputStream inputStream = csvService.getPatientCSVStream(data.getColumns(), data.getBody());
			HashMap<String, Object> result = sftpService.uploadCSVToSFTP(data, inputStream);
			if (result.get("errorMessage") != null) {
				return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error downloading patient csv: {}", e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@PostMapping(value = "/send-csv-to-email")
	public ResponseEntity<Object> sendCSVToEmail(@RequestBody CSVExportDTO data) {
		try {
			InputStream inputStream = csvService.getPatientCSVStream(data.getColumns(), data.getBody());
			HashMap<String, Object> result = emailService.sendCSVToEmail(data, inputStream);
			if (result.get("errorMessage") != null) {
				return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
			}
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error sending patient csv to {}: {}", data.getEmail(), e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}
}
