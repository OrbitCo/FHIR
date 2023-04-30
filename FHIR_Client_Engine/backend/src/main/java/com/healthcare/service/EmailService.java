package com.healthcare.service;

import com.healthcare.dto.CSVExportDTO;
import com.palmetto.fhir.utilities.email.EmailMessage;
import com.palmetto.fhir.utilities.email.EmailSender;
import com.palmetto.fhir.utilities.email.SMTPBean;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

@Service
public class EmailService {

    @Value("${email.from}")
    private String FROM_EMAIL;

    @Value("${email.host}")
    private String HOST;

    public HashMap<String, Object> sendCSVToEmail(CSVExportDTO dto, InputStream csvData) throws Exception {

        HashMap<String, Object> result = new HashMap<>();

        SMTPBean smtpBean = new SMTPBean();
        smtpBean.setHost(HOST);
        smtpBean.setPort(587);

        EmailSender emailSender = new EmailSender(smtpBean);

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.addTo(dto.getEmail().getTo());
        emailMessage.setFrom(FROM_EMAIL);
        emailMessage.setSubject(dto.getEmail().getSubject());
        emailMessage.setBody(dto.getEmail().getBody());

        String tmpdir = System.getProperty("java.io.tmpdir");
        File csvFile = new File(tmpdir + "/" + UUID.randomUUID() + ".csv");
        FileUtils.copyInputStreamToFile(csvData, csvFile);

        emailMessage.addAttachment(Paths.get(csvFile.getAbsolutePath()));
        try {
            emailSender.sendMessage(emailMessage);
            result.put("result", "success");
            result.put("message", "CSV has been sent successfully to " + dto.getEmail());
        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("result", "failed");
            result.put("errorMessage", "Failed to send CSV to " + dto.getEmail() + ": " + ex.getMessage());
        }


        if (csvFile.exists()) {
            csvFile.delete();
        }

        return result;
    }

}
