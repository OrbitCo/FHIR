package com.healthcare.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.util.BundleBuilder;
import com.healthcare.dto.APIResponse;
import com.healthcare.dto.QueriesDTO;
import com.healthcare.dto.QueryDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FHIRService {

    public static String ErrorMessage = "";

    @Autowired
    private AuthorizationService authorizationService;

    private FhirContext ctx = FhirContext.forR4();
    private IParser parser = ctx.newJsonParser();

    public APIResponse getResultAuhorizedAPI(String requestURI) throws IOException {
        Response response = executeAuthRequest(requestURI);
        String apiResponse = getResponseBody(response);
        return new APIResponse(HttpStatus.OK.value(), "Result for URI: " + requestURI, apiResponse, null);
    }

    public Response executeAuthRequest(String url) throws IOException {
        if(authorizationService.getAccessToken().equals("")) { //If there is no token, we should try this with no auth.
            return executeNoAuthRequest(url);
        }
        OkHttpClient client = buildHttpClient();
        Request request = buildGetRequestWithAuth(url);
        //log.info("generate request " + request.toString() + " header "+request.headers().toString());
        return client.newCall(request).execute();
    }

    public Response executeNoAuthRequest(String url) throws IOException {
        OkHttpClient client = buildHttpClient();
        Request request = buildGetRequestWithNoAuth(url);
        //log.info("generate request " + request.toString() + " header "+request.headers().toString());
        return client.newCall(request).execute();
    }

    OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String getResponseBody(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        if (StringUtils.isBlank(responseBody)) {
            throw new RuntimeException("Empty response body is returned by the HAPI API");
        }
        return responseBody;
    }

    Request buildGetRequestWithAuth(String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .addHeader("Authorization", "Bearer " + authorizationService.getAccessToken())
                .get();
        return builder.build();
    }

    Request buildGetRequestWithNoAuth(String url) {
        Request.Builder builder = new Request.Builder().url(url)
                .get();
        return builder.build();
    }

    public APIResponse processIncomingQueries(QueriesDTO queries) throws IOException {
        this.ErrorMessage = "";
        return executeQueries(queries, "", 0, HttpStatus.OK.value());
    }

    /**
     * A recursive function to cycle through all the incoming queries.
     * @param queries The QueryDTO containing an array of queries
     * @param incomingBody The response of the previous query (empty string to start)
     * @param index The index of our current query
     * @param myStatus the HTTP status code to return.
     * @return The final responseBody.
     */
    private APIResponse executeQueries(QueriesDTO queries, String incomingBody, int index, int myStatus) throws IOException {
        if(index >= queries.getQueries().length) {
            return new APIResponse(myStatus, "Result for Queries: " + queries,
                    incomingBody, this.ErrorMessage);
        }

        FhirContext ctx = FhirContext.forR4();
        FHIRPathEngine myEng = new FHIRPathEngine(new SimpleWorkerContext());
        QueryDTO currentQ = queries.getQueries()[index];

        //Lets authenticate if we need to
        if(currentQ.getAuthentication() != null) {
            if(currentQ.getAuthentication().getAuthenticationType().equals("NoAuth")) {
                //If the user specified NoAuth, we should flush credentials.
                authorizationService.flushAccessToken();
            } else {
                authorizationService.fetchToken(currentQ.getAuthentication());
            }
        }

        //Try to fetch the capability statement.  If we fail, or the version isn't R4, give a warning back to the user.
        try {
            if(!fetchCapabilityStatement(currentQ).getFhirVersion().toString().replace("_","").startsWith("4")) {
                myStatus = HttpStatus.PRECONDITION_FAILED.value();
                this.ErrorMessage = "FHIR Server did not match expected version (R4).";
            }

        } catch(Exception e) {
            log.debug(e.getMessage());
            //Return a 412 Status code instead
            myStatus = HttpStatus.PRECONDITION_FAILED.value();
            this.ErrorMessage = "FHIR Server did not match expected version (R4).";
        }

        //Filter out the return value from the incomingBody via FHIR PATH
        String myParams = "";
        if(index > 0) { //this is not the first call
            if(currentQ.getReturnValue() != null) { //If we have a specified return value
                Bundle incomingBundle = parser.parseResource(Bundle.class, incomingBody);
                String providedReturnValue = currentQ.getReturnValue();
                log.debug(providedReturnValue);
                // Patient.id -> Bundle.entry.select(resource as <Patient>)<.id>
                String goodFHIRPath = "Bundle.entry.select(resource as " +
                        providedReturnValue.split("\\.")[0] + //Needs escape \. because split is a REGEX
                        ")" + providedReturnValue.substring(providedReturnValue.indexOf("."));
                log.info("FHIR Path: " + goodFHIRPath);
                List<Base> results = myEng.evaluate(incomingBundle, goodFHIRPath);
                log.info("Results of FHIR Path: " + results);
                return executeQueries(queries, buildBundleFromList(results, currentQ), index+1, myStatus);
            }
        }
        //Take the currentQ.getQuery() string and replace and <<tokens>> with the value from myParams
        currentQ.setQuery(currentQ.getQuery().replaceAll("<<.*>>", myParams));

        //Make the actual query
        String requestURI = currentQ.getConnection() + currentQ.getQuery();
        Response response = executeAuthRequest(requestURI);
        String apiResponse = getResponseBody(response);
        log.info(requestURI);
        //log.info(apiResponse);

        String newBody = apiResponse.replaceAll("<p>", "<div>").replaceAll("</p>", "</div>");
        Bundle newBundle = new Bundle();
        try {
            newBundle = parser.parseResource(Bundle.class, newBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIResponse(500, "Could not parse returned result from" + requestURI,
                    newBody, this.ErrorMessage);
        }


        //Include pagination
        while(newBundle.getLink(IBaseBundle.LINK_NEXT) != null) { //While there is a next page
            //Get the next bundle
            Bundle nextBundle = getNextBundle(currentQ, newBundle);
            //Add it's entries to our current bundle
            for(Bundle.BundleEntryComponent b : nextBundle.getEntry()) {
                newBundle.addEntry(b);
            }
            //Set our 'next' link to it's next link
            ArrayList<Bundle.BundleLinkComponent> newLinks = new ArrayList<Bundle.BundleLinkComponent>();
            newLinks.add(newBundle.getLink(IBaseBundle.LINK_SELF));
            if(nextBundle.getLink(IBaseBundle.LINK_NEXT) != null) {
                newLinks.add(nextBundle.getLink(IBaseBundle.LINK_NEXT));
            }
            newBundle.setLink(newLinks);
        }
        newBundle.setTotal(newBundle.getEntry().size());
        //Call recursively
        return executeQueries(queries, parser.encodeResourceToString(newBundle), index+1, myStatus);
    }

    /**
     * Strips and obtains the ID from a url like allscripts/fhir/Patient/ID/_history/1
     * @param b
     * @return
     */
    private String getIdFromURL(Base b) {
        //Remove _history tail
        String itemURL = b.toString().split("/_history")[0];
        //Get everything after the last slash (whatever/fhir/Patient/_id_)
        itemURL = itemURL.substring(itemURL.lastIndexOf("/") + 1);
        return itemURL;
    }

    /**
     * Fetches the capability statement by making a call to connection + metadata and parsing the response.
     * @param currentQ THe query to get the connection from.
     * @return The CapabilityStatement (HAPI FHIR object)
     * @throws IOException If the returned object is _not_ a CapabilityStatement, this throws IOException (can't parse)
     */
    private CapabilityStatement fetchCapabilityStatement(QueryDTO currentQ) throws IOException {
        String requestURI = currentQ.getConnection() + "metadata";
        Response response = executeNoAuthRequest(requestURI);
        String apiResponse = getResponseBody(response);
        CapabilityStatement capability = parser.parseResource(CapabilityStatement.class, apiResponse);
        log.info("FHIR Version: " + capability.getFhirVersion());
        return capability;
    }

    /**
     * If the target doesn't support ',' OR seperators, we're going to have to build this bundle from scratch.
     * 1: Create an empty bundle to store results.
     * 2: Execute each individual query, and add the 'entry' of the results to our bundle
     * 3: Return that bundle as a string.
     * This should allow us to effectively 'mock' what the output would be if the server supported comma OR
     * @param results The list of parameters to query
     * @param currentQ The current query (for our connection and query, to insert the results into)
     * @return A bundle mocking what the FHIR server would return on an OR search
     */
    private String buildBundleFromList(List<Base> results, QueryDTO currentQ) throws IOException {
        int resultsTotal = results.size();
        log.debug("Parameters to process: " + resultsTotal);
        BundleBuilder myBundle = new BundleBuilder(ctx);
        myBundle.setType(Bundle.BundleType.SEARCHSET.toCode());
        int processed = 0;
        int size = 0;
        if(resultsTotal == 0) {
            this.ErrorMessage = "This FHIR Path yielded 0 results to search on.";
        }
        for(Base b : results) {
            //Make the individual query
            String currentId = getIdFromURL(b);
            //Replace the <<token>> with this Id.  We shouldn't overwrite currentQ because we'll be doing this again.
            String currentQuery = currentQ.getQuery().replaceAll("<<.*>>", currentId);
            //Make the request
            String requestURI = currentQ.getConnection() + currentQuery;
            Response response = executeAuthRequest(requestURI);
            String apiResponse = getResponseBody(response);
            //Turn the response into an individual resource(s)
            apiResponse = apiResponse.replaceAll("<p>", "<div>").replaceAll("</p>", "</div>");
            Bundle resultingBundle = parser.parseResource(Bundle.class, apiResponse);
            boolean hasNextPage;
            do { //a do-while so it always runs at least once.
                for(Bundle.BundleEntryComponent entry : resultingBundle.getEntry()) {
                    //Add them to the bundle
                    IBase currentEntry = myBundle.addEntry();
                    myBundle.addToEntry(currentEntry, "fullUrl", entry.getFullUrlElement());
                    myBundle.addToEntry(currentEntry, "resource", entry.getResource());
                    size++;

                }
                //Get the next page and repeat the process
                hasNextPage = resultingBundle.getLink(IBaseBundle.LINK_NEXT) != null;
                if(hasNextPage) {
                    resultingBundle = getNextBundle(currentQ, resultingBundle);
                }
            } while (hasNextPage);
            processed++;
            if(processed % 20 == 0) {
                log.debug("..progress: " + processed + "/" + resultsTotal + " (" + ((processed * 100)/resultsTotal) + "%)");
            }
        }
        //Return it as if it had come from a FHIR server
        myBundle.setBundleField("total", String.valueOf(size));
        return parser.encodeResourceToString(myBundle.getBundle());
    }

    /**
     * A helper method to return the next page of a given bundle (using Link relation 'next')
     * @param currentQ The query we're executing.  This is used to put in the connection if the Bundle says the link is "localhost"
     *                 (which, hot tip, our Smile server does, if you call to port 8000.  Dunno what's up with that.)
     * @param resultingBundle
     * @return
     * @throws IOException
     */
    private Bundle getNextBundle(QueryDTO currentQ, Bundle resultingBundle) throws IOException {
        String nextRequestURI = resultingBundle.getLink(IBaseBundle.LINK_NEXT).getUrl().replaceAll("http.*:\\/\\/localhost:\\d{4}", currentQ.getConnection());
        Response nextResponse = executeAuthRequest(nextRequestURI);
        String nextApiResponse = getResponseBody(nextResponse);
        String nextBody = nextApiResponse.replaceAll("<p>", "<div>").replaceAll("</p>", "</div>");
        return parser.parseResource(Bundle.class, nextBody);
    }
}
