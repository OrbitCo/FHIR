package com.healthcare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import security.EncryptPassword;

import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Configuration
public class JpaConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource()
    {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(env.getProperty("spring.datasource.url"));
        dataSourceBuilder.username(env.getProperty("spring.datasource.username"));
        try {
            dataSourceBuilder.password(this.decryptPassword());
        } catch (Exception e) {
            System.out.println("Password retrieval failed: did you set the environment variables?");
            e.printStackTrace();
            //If we don't have a good password, I don't want to try to login
            throw new RuntimeException();
        }

        return dataSourceBuilder.build();
    }

    private String decryptPassword() throws GeneralSecurityException, IOException {
        String encodedKey = System.getenv("USRFHIAQ_encoded_key");
        String encryptedPassword = System.getenv("USRFHIAQ_encrypted_password");

        SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), "AES");
        String password = EncryptPassword.decrypt(encryptedPassword, key);

        return password;
    }
}