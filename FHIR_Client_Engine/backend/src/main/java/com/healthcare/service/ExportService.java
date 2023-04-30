package com.healthcare.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.healthcare.dto.APIResponse;
import com.healthcare.dto.FHIRExportDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ExportService {
    @Autowired
    private AuthorizationService authorizationService;

    private FhirContext ctx = FhirContext.forR4();
    private IParser parser = ctx.newJsonParser();

    public APIResponse importDataToClient(FHIRExportDTO fhirExportDTO) throws IOException {
        //Authenticate with target server
        if(!fhirExportDTO.getRequest().getAuthentication().getAuthenticationType().equals("NoAuth")) {
            this.authorizationService.fetchToken(fhirExportDTO.getRequest().getAuthentication());
        }
        //TODO: Flush token otherwise?

        Bundle bundle = parser.parseResource(Bundle.class, fhirExportDTO.getBody());

        if(fhirExportDTO.getType().equals("BATCH")) {
            bundle.setType(Bundle.BundleType.BATCH);
        } else {
            bundle.setType(Bundle.BundleType.TRANSACTION);
        }

        List<Bundle.BundleEntryComponent> entriesNew = bundle.getEntry();
        for (Bundle.BundleEntryComponent entryData : entriesNew) {
            entryData.getRequest().setMethod(org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST).setUrl(entryData.getResource().getResourceType().toString() + "/" + entryData.getResource().getIdPart());
            entryData.setSearch(null);
        }

        FhirContext ctx = FhirContext.forR4();
        JSONObject jsonPayload = new JSONObject(ctx.newJsonParser().encodeResourceToString(bundle));

        System.out.println(jsonPayload);

        String CLIENT_URL = fhirExportDTO.getRequest().getConnection();

        System.out.println(CLIENT_URL);

        //IGenericClient client = ctx.newRestfulGenericClient(uri);
        //Bundle resp = client.transaction().withBundle(bundle).execute();

        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, jsonPayload.toString());
            Request request;
            Request.Builder requestB = new Request.Builder()
                    .url(CLIENT_URL)
                    .post(body);
            if(!fhirExportDTO.getRequest().getAuthentication().getAuthenticationType().equals("NoAuth")) {
                requestB.addHeader("Authorization", "Bearer " + authorizationService.getAccessToken());
            }
            request = requestB.build();

            Response response = client.newCall(request).execute();
            //System.out.println(response.body().string());

            String apiResponse = getResponseBody(response);
            return new APIResponse(HttpStatus.OK.value(), "Result for URI: " + CLIENT_URL, apiResponse, null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while sending patient data to client: " + e.getMessage());
            return new APIResponse(HttpStatus.EXPECTATION_FAILED.value(), null, null, e.getMessage());
        }
    }

    private String getResponseBody(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        if (StringUtils.isBlank(responseBody)) {
            throw new RuntimeException("Empty response body is returned by the HAPI API");
        }
        return responseBody;
    }
}
