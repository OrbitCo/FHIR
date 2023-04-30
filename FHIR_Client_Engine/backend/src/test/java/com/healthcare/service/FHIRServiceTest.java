package com.healthcare.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.healthcare.dto.QueriesDTO;
import com.healthcare.dto.QueryDTO;
import junit.framework.TestCase;
import okhttp3.Request;
import okhttp3.*;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FHIRServiceTest extends TestCase {
    public FHIRServiceTest() {
        super();
    }
    QueriesDTO mockQueries;
    int queryCount;
    FHIRService service;
    private FhirContext ctx = FhirContext.forR4();
    private IParser parser = ctx.newJsonParser();

    /**
     * Resets FHIRService, the mockQueries object, and the query count before each test,
     * to ensure a clean run.
     */
    @BeforeEach
    protected void setUp() {
        service = spy(FHIRService.class);
        mockQueries = new QueriesDTO();
        queryCount = 0;
    }

    /**
     * A simple single query that should return a bundle with three resources and a "next" link,
     * another bundle with three resources& a "next" link, and then a last bundle with just three resources.
     * The return bundle should have all nine resources in one page, to test that our method properly paginates and
     * combines.
     * @see <a href="https://hapifhir.io/hapi-fhir/docs/server_plain/paging.html">FHIR Paging</a>
     * @throws IOException In theory, executeAuthRequest could throw an IOException. In practice, it shouldn't
     *  because we're mocking it.
     */
    @Test
    public void test__SingleQueryThreePages_returnsAllResourcesInOneBundle() throws IOException {
        //Initialize the initial QueriesDTO Object with just one query
        QueryDTO Q1 = createQueryDTO();
        Q1.setType("full");
        mockQueries.setQueries(new QueryDTO[] {Q1});
        createThreePageBundle(Q1, createLocations());
        checkLocationBundle();
    }

    /**
     * Similar to above, but the bundle from the first test is the return of a FHIR Path taken instead.
     * This is worth testing because the paging logic is different depending on if it was the initial query
     * or a nested query.  This is because what we do with those two resulting bundles is slightly different:
     *  -> Single Query: Take the first bundle and append subsequent results, return it.
     *  -> Nested Query: Construct a new bundle and add each result as an entry.  Return the constructed bundle.
     * @throws IOException
     */
    @Test
    public void test__NestedQueryWithThreePages_returnsFHIRPathResourcesInOneBundle() throws IOException {
        QueryDTO Q1 = createQueryDTO();
        QueryDTO Q2 = createQueryDTO();
        String baseQ2Query = Q2.getQuery();
        Q2.setQuery(baseQ2Query + "?_id=<<token>>");
        Q2.setReturnValue("Patient.id");
        Q2.setType("full");

        createBundleAtAddress(service, Q1, new Resource[] {new Patient().setId("MockPatient-01")},
                1, false);

        //In this case, we want the pages to be in the second nested query to make sure it works in the nested portion.
        createThreePageBundle(Q2, createLocations(), Q2.getConnection() + baseQ2Query + "?_id=MockPatient-01");
        mockQueries.setQueries(new QueryDTO[] {Q1, Q2});

        checkLocationBundle();
    }

    /**
     * Very similar to above, but without pages.  Instead, FHIR Path provides nine single-result queries to bundle:
     * Three claims map to nine Claims map to nine Locations.  Same resulting bundle as the rest (should be).
     * @throws IOException
     */
    @Test
    public void test__ThreeNestedQueries_returnsFHIRPathResourcesInOneBundle() throws IOException {
        QueryDTO Q1 = createQueryDTO();
        QueryDTO Q2 = createQueryDTO();
        QueryDTO Q3 = createQueryDTO();
        String baseQ2Query = Q2.getQuery();
        Q2.setQuery(baseQ2Query + "?patient=<<token>>");
        Q2.setReturnValue("Patient.id");
        String baseQ3Query = Q3.getQuery();
        Q3.setQuery(baseQ3Query + "?identifier=<<token>>");
        Q3.setReturnValue("Claim.identifier.value");
        Q3.setType("full");

        //Create the three patients
        createBundleAtAddress(service, Q1, new Resource[] {
                        new Patient().setId("MockPatient-01"),
                        new Patient().setId("MockPatient-02"),
                        new Patient().setId("MockPatient-03")
                },
                1, false);

        //Create the claims (three per patient)
        createClaimsAtAddress(Q2, baseQ2Query, 1);
        createClaimsAtAddress(Q2, baseQ2Query, 2);
        createClaimsAtAddress(Q2, baseQ2Query, 3);

        //Create the nine locations (one for each Claim)
        for(Location[] lArry : createLocations()) {
            for(Location l : lArry) {
                createBundleAtAddress(service, Q3, new Location[] {l}, 1, false,
                        Q3.getConnection() + baseQ3Query + "?identifier=" + l.getIdentifier().get(0).getValue());
            }
        }

        mockQueries.setQueries(new QueryDTO[] {Q1, Q2, Q3});

        checkLocationBundle();
    }

    /**
     * A convenience method to create nine locations.  These are often used as the expected return bundle for tests.
     * @return a 2d array of 9 locations, split as 3 per 1d array.
     */
    private Location[][] createLocations() {
        return new Location[][] {
                new Location[] {createLocation("001-001"),
                createLocation("001-002"),
                createLocation("001-003")},
                new Location[] {createLocation("002-001"),
                        createLocation("002-002"),
                        createLocation("002-003")},
                new Location[] {createLocation("003-001"),
                        createLocation("003-002"),
                        createLocation("003-003")}};
    }

    /**
     * Takes in a 2d array and makes it into three different bundles, each linking to each other with the "next"
     * link relaiton (except the last one)
     * @param myQ The query that should lead to the first page in the bundle.
     * @param myResources The 2d array to be split into the bundles (each 1d array within becomes a page, in order)
     * @param initialLink The link that should be used to access the first page.  None might be provided (see overload below)
     * @throws IOException
     */
    private void createThreePageBundle(QueryDTO myQ, Resource[][] myResources, String initialLink) throws IOException {
        createBundleAtAddress(service,
                myQ,
                myResources[0],
                1,
                true,
                initialLink);
        createBundleAtAddress(service,
                myQ,
                myResources[1],
                2,
                true);
        createBundleAtAddress(service,
                myQ,
                myResources[2],
                3,
                false);
    }

    /**
     * @Overload default method for no link supplied - just uses the QueryDTO.connection and QueryDTO.query as default.
     */

    private void createThreePageBundle(QueryDTO myQ, Resource[][] myResources) throws IOException {
        this.createThreePageBundle(myQ, myResources, myQ.getConnection() + myQ.getQuery());

    }

    /**
     * A convenience method for quickly creating mock QueryDTOs.
     * @return
     */
    private QueryDTO createQueryDTO() {
        this.queryCount++;
        QueryDTO newQ = new QueryDTO();
        newQ.setOrder(queryCount);
        newQ.setConnection("MOCK/");
        newQ.setQuery("MOCK" + queryCount);
        newQ.setType("nested");
        return newQ;
    }

    /**
     * Needed to corrected mock executeAuthRequest, which returns a Response object.  This is what should be passed in
     * to the mockito doReturn().when().executeAuthRequest()
     * @param content The JSON Body to be returned.
     * @return
     */
    private Response createHTTPResponse(JSONObject content) {
        Response.Builder responseBuilder = new Response.Builder();
        responseBuilder.code(200);
        responseBuilder.body(ResponseBody.create(MediaType.parse("text/json"), content.toString()));
        Request mockRequest = new Request.Builder()
                .url("https://mockito.com")
                .build();
        responseBuilder.request(mockRequest);
        responseBuilder.protocol(Protocol.HTTP_2);
        return responseBuilder.build();
    }

    /**
     * Create the bundle that needs to be returned by the Query's URL
     * Why not just create a HAPI FHIR Bundle?  Why a JSONObject?
     * -> Because as far as I can tell, there is no way to add the "link" section to the HAPI FHIR Bundle.
     * @param fs FHIRService.  A mocked class that is set to return the bundle when the executeAuthRequest is called.
     * @param query The query we want to return this bundle.
     * @param resources The array of resources to appear as the "entry"
     * @param pageNumber Which page this is (starts at 1)
     * @param hasNext has a page after this one
     * @exception IOException In theory, executeAuthRequest could throw an exception.  In practice, it's mocked.
     */
    private void createBundleAtAddress(FHIRService fs, QueryDTO query, Resource[] resources,
                                       int pageNumber, boolean hasNext, String myLink) throws IOException {
        JSONObject response = new JSONObject();
        response.put("resourceType", "Bundle");
        response.put("type", "searchset");
        ArrayList<JSONObject> myLinks = new ArrayList<JSONObject>();
        JSONObject selfLink = new JSONObject();
        selfLink.put("relation", "self");
        selfLink.put("url", myLink);
        myLinks.add(selfLink);
        if(hasNext) {
            JSONObject nextLink = new JSONObject();
            nextLink.put("relation", "next");
            nextLink.put("url", query.getConnection() + query.getQuery() + "/Page" + (pageNumber+1));
            myLinks.add(nextLink);
        }
        response.put("link", myLinks);
        ArrayList<JSONObject> myEntries = new ArrayList<JSONObject>();
        for(Resource b: resources) {
            JSONObject resourceAsJSON = new JSONObject(parser.encodeResourceToString(b));
            JSONObject containingObject = new JSONObject();
            containingObject.put("fullUrl", myLink + "/mockwhatever");
            containingObject.put("resource", resourceAsJSON);
            myEntries.add(containingObject);
        }
        response.put("entry", myEntries);
        doReturn(createHTTPResponse(response)).when(fs).executeAuthRequest(
                myLink);
    }

    /**
     * @Overload default method for no link supplied - just uses QueryDTO.getConnection + QueryDTO.getQuery + /Page + pageNumber
     */

    private void createBundleAtAddress(FHIRService fs, QueryDTO query, Resource[] resources,
                                       int pageNumber, boolean hasNext) throws IOException {
        String myLink = (pageNumber > 1) ? query.getConnection() + query.getQuery() + "/Page" + pageNumber
                : query.getConnection() + query.getQuery();
        this.createBundleAtAddress(fs, query, resources, pageNumber, hasNext, myLink);
    }

    /**
     * Convenience method for creating a Location with the provided identifier.
     * @param identifierValue The location.identifier.value
     * @return
     */
    private Location createLocation(String identifierValue) {
        Location loc = new Location();
        Address add1 = new Address();
        add1.setCity("CHARLESTON");
        add1.setState("SC");
        loc.setAddress(add1);
        ArrayList<Identifier> ids1 = new ArrayList<Identifier>();
        Identifier ide1 = new Identifier();
        ide1.setSystem("https://palmettogba.com/systems/BusinessIdentifier");
        ide1.setValue(identifierValue);
        ids1.add(ide1);
        loc.setIdentifier(ids1);
        return loc;
    }

    /**
     * A convenience method for creating a bundle of three claims with Claim.identifier.value pattern matching Location
     * @param Q The Query that should result in these Claims
     * @param baseQ2Query The QueryDTO.getQuery() that should result in these Claims.  We shouldn't use Q.getQuery() because
     *                    it could have been altered to include a \<\<token\>\>, which we don't want.
     * @param correlation The integer to put at 00X-001 to make these Claims.identifier.value match a Location.identifier.value.
     * @throws IOException
     */
    private void createClaimsAtAddress(QueryDTO Q, String baseQ2Query, int correlation) throws IOException {
        createBundleAtAddress(service, Q, new Resource[] {
                        new Claim().setIdentifier(Arrays.asList(new Identifier[]{
                                new Identifier().setValue("00" + correlation + "-001")
                        })),
                        new Claim().setIdentifier(Arrays.asList(new Identifier[]{
                                new Identifier().setValue("00" + correlation + "-002")
                        })),
                        new Claim().setIdentifier(Arrays.asList(new Identifier[]{
                                new Identifier().setValue("00" + correlation + "-003")
                        })),
                },
                1, false, Q.getConnection() + baseQ2Query + "?patient=MockPatient-0" + correlation);
    }

    /**
     * Runs JUnit assertions against the Location bundle that our tests expect to return.
     * Checks for length of 9, that the first entry has Location.identifier.value of 001-001,
     *  and the last entry has Location.identifier.value of 003-003.
     */
    private void checkLocationBundle() {
        Bundle outputBundle = new Bundle();
        try {
            outputBundle = parser.parseResource(Bundle.class, service.processIncomingQueries(mockQueries).getData().toString());

            System.out.println("Bundle returned: ");
            System.out.println(parser.encodeResourceToString(outputBundle));
            //Assertions
            assertEquals("Bundle does not have expected total of entries!", 9, outputBundle.getTotal()); //Three pages, three resources per page.
            //Resources should appear in predictable order (as they appear)
            Location loc1 = (Location) outputBundle.getEntry().get(0).getResource();
            assertEquals("First resource was not as expected, by identifier!", "001-001", loc1.getIdentifier().get(0).getValue());
            Location loc9 = (Location) outputBundle.getEntry().get(8).getResource();
            assertEquals("Last resource was not as expected, by identifier!", "003-003", loc9.getIdentifier().get(0).getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}