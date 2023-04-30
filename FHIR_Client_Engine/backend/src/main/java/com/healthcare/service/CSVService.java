package com.healthcare.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CSVService {

    private final IParser FHIR_PARSER = FhirContext.forR4().newJsonParser();

    public void downloadPatientCSV(PrintWriter writer, List<String> headers, String patientJSON) {

        try {

            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            writePatientDataToCSV(csvWriter, headers, patientJSON);
            csvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getPatientCSVStream(List<String> headers, String patientJSON) {
        try {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(stream);

            CSVWriter csvWriter = new CSVWriter(streamWriter,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            writePatientDataToCSV(csvWriter, headers, patientJSON);
            streamWriter.flush();

            return new ByteArrayInputStream(stream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void downloadPatientCSVBasicAuth(PrintWriter writer, List<String> headers, String patientJSON) {
        try {

            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            writePatientDataToCSV(csvWriter, headers, patientJSON);
            csvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writePatientDataToCSV(CSVWriter csvWriter, List<String> headers, String patientJSON) throws IOException {

        String[] headerArr = new String[headers.size()];
        csvWriter.writeNext(headers.toArray(headerArr));

        Bundle bundle = FHIR_PARSER.parseResource(Bundle.class, patientJSON);
        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();

        FHIRPathEngine fhirEngine = new FHIRPathEngine(new SimpleWorkerContext());

        for (Bundle.BundleEntryComponent entryData : entries) {

            List<String> columns = new ArrayList<>();
            Resource resource = entryData.getResource();

            for (String fhirpath : headers) {
                List<Base> results = fhirEngine.evaluate(resource, fhirpath);
                String value = "";
                for (Base base : results) {
                    if (StringUtils.endsWithIgnoreCase(base.fhirType(), "date")) {
                        new SimpleDateFormat("MM/dd/yyyy").format(base.castToDate(base).getValue());
                    } else {
                        value += base.primitiveValue() + " ## ";
                    }
                }
                if (StringUtils.isNotEmpty(value)) {
                    value = value.substring(0, value.length() - 4);
                }
                columns.add(value.toString());
            }

            String[] fieldsArr = new String[columns.size()];
            csvWriter.writeNext(columns.toArray(fieldsArr));
        }
    }

    public List<String> getAllPathsFromJSON(String json) {
        JSONObject object = new JSONObject(json);
        String jsonPath = "";
        List<String> pathList = new ArrayList<>();
        if (json != JSONObject.NULL) {
            readJSONObject(object, jsonPath, pathList);
        }
        return pathList;
    }

    private void readJSONObject(JSONObject object, String jsonPath, List<String> pathList) {
        Iterator<String> keysItr = object.keys();
        String parentPath = jsonPath;
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
            if (StringUtils.isNotEmpty(parentPath)) {
                jsonPath = parentPath + ".";
            }
            jsonPath += key;

            if (value instanceof JSONArray) {
                readJsonArray((JSONArray) value, jsonPath, pathList);
            } else if (value instanceof JSONObject) {
                readJSONObject((JSONObject) value, jsonPath, pathList);
            } else { // is a value
                if (jsonPath.contains("entry.resource.")) {
                    pathList.add(jsonPath.substring("entry.resource.".length(), jsonPath.length()));
                }
            }
        }
    }

    private void readJsonArray(JSONArray array, String jsonPath, List<String> pathList) {
        String parentPath = jsonPath;
        // Considering first object only since bundle will have all same type of objects
        Object value = array.get(0);
        jsonPath = parentPath;

        if (value instanceof JSONArray) {
            readJsonArray((JSONArray) value, jsonPath, pathList);
        } else if (value instanceof JSONObject) {
            readJSONObject((JSONObject) value, jsonPath, pathList);
        } else { // is a value
            if (jsonPath.contains("entry.resource.")) {
                pathList.add(jsonPath.substring("entry.resource.".length(), jsonPath.length()));
            }
        }
    }
}
