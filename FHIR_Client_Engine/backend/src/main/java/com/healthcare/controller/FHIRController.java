package com.healthcare.controller;

import com.healthcare.dto.APIResponse;
import com.healthcare.dto.QueriesDTO;
import com.healthcare.service.FHIRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/fhir", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class FHIRController {

    @Autowired
    private FHIRService fhirService;

    @PostMapping(value = "/get-authorized-api-result",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public APIResponse getAuthorizedAPIResult(@RequestBody QueriesDTO incomingQueries ) throws IOException {
        try {
            return fhirService.processIncomingQueries(incomingQueries);
            //return fhirService.getResultAuhorizedAPI(requestURI);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIResponse(500, "INTERNAL SERVER ERROR",
                    "{ \"message\": \"" + e.getMessage() + "\" }", fhirService.ErrorMessage);
        }
    }
}
