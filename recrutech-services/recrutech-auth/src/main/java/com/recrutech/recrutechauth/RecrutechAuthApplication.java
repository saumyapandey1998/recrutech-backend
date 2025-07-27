package com.recrutech.recrutechauth;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class RecrutechAuthApplication {

    public static void main(String[] args) {
        // Register BouncyCastle provider at application startup
        Security.addProvider(new BouncyCastleProvider());
        
        SpringApplication.run(RecrutechAuthApplication.class, args);
    }

}
