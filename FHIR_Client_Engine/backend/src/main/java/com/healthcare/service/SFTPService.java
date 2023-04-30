package com.healthcare.service;

import com.healthcare.dto.CSVExportDTO;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Service
public class SFTPService {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    private static Session getSFTPChannel(String host, String port, String username, String password, String pemfile) throws JSchException {
        JSch jsch = new JSch();
        if (pemfile != null) {
            jsch.addIdentity(pemfile);
        }

        Properties config = new Properties();
        config.put("trust", "true");
        config.put("StrictHostKeyChecking", "no");
        config.put("HashKnownHosts", "yes");

        Session jschSession = jsch.getSession(username, host, Integer.parseInt(port));

        jschSession.setConfig(config);

        if (StringUtils.isNotBlank(password)) {
            jschSession.setPassword(password);
        }

        jschSession.setTimeout(SESSION_TIMEOUT);
        jschSession.connect();
        return jschSession;
    }

    public HashMap<String, Object> uploadCSVToSFTP(CSVExportDTO dto, InputStream csvData) {
        HashMap<String, Object> result = new HashMap<>();

        Session jschSession = null;
        try {
            String tmpdir = System.getProperty("java.io.tmpdir");
            File ppkFile = new File(tmpdir + "/" + UUID.randomUUID() + ".ppk");

            byte[] byteArray = Base64.decodeBase64(dto.getSftp().getPrivateKeyBase64());
            FileOutputStream fos = new FileOutputStream(ppkFile);
            fos.write(byteArray);
            fos.close();

            String remoteFile = dto.getSftp().getTargetDirectory() + "/" + dto.getFileName() + ".csv";

            File csvFile = new File(tmpdir + "/" + UUID.randomUUID() + ".csv");
            FileUtils.copyInputStreamToFile(csvData, csvFile);

            //********* Keeping this code since utility SFTP package is not supporting copy local file to remote only remote to local is available ********

            /*SFTPBean sftpBean = new SFTPBean();
            String home = String.valueOf(System.getenv("HOME"));
            String knownHostsFileName = Paths.get(home, ".ssh", "known_hosts").toString();
            if (knownHostsFileName != null && new File(knownHostsFileName).exists()) {
                sftpBean.setKnownHostsPath(knownHostsFileName);
            } else {
                sftpBean.setKnownHostsPath(System.getProperty("user.home"));
            }
            sftpBean.setRemoteHost(dto.getSftpHost());
            sftpBean.setRemotePort(Integer.parseInt(dto.getSftpPort()));
            sftpBean.setUsername(dto.getSftpUser());
            sftpBean.setPrivateKeyPath(ppkFile.getAbsolutePath());

            SFTPRetriever sftpRetriever = new SFTPRetriever(sftpBean);
            sftpRetriever.copyRemoteFile(targetFile.getAbsolutePath(), remoteFile);*/


            jschSession = getSFTPChannel(dto.getSftp().getServerAddress(),
                    dto.getSftp().getServerPort(),
                    dto.getSftp().getUsername(),
                    dto.getSftp().getPassword(), ppkFile.getAbsolutePath());

            Channel sftp = jschSession.openChannel("sftp");

            // 5 seconds timeout
            sftp.connect(CHANNEL_TIMEOUT);
            ChannelSftp channelSftp = (ChannelSftp) sftp;
            // transfer file from local to remote server
            channelSftp.put(csvFile.getAbsolutePath(), remoteFile);

            channelSftp.exit();

            result.put("result", "success");
            result.put("message", "File " + dto.getFileName() + ".csv has been uploaded successfully");

            if (ppkFile.exists()) {
                ppkFile.delete();
            }
            if (csvFile.exists()) {
                csvFile.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", "failed");
            result.put("errorMessage", "Error uploading file " + dto.getFileName() + ".csv: " + e.getMessage());
        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
        return result;
    }
}