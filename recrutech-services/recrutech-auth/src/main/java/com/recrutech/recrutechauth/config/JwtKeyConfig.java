package com.recrutech.recrutechauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

/**
 * Configuration for JWT key.
 * This class provides RSA keys for JWT signing and verification.
 */
@Configuration
public class JwtKeyConfig {

    @Value("${jwt.public-key:#{null}}")
    private Resource publicKeyResource;

    @Value("${jwt.private-key:#{null}}")
    private Resource privateKeyResource;

    /**
     * Creates an RSA key pair for JWT signing and verification.
     * If public and private keys are provided in the configuration, they will be used.
     * Otherwise, a new key pair will be generated.
     *
     * @return the RSA key pair
     */
    @Bean
    public KeyPair keyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (publicKeyResource != null && privateKeyResource != null) {
            // Load keys from resources
            RSAPublicKey publicKey = loadPublicKey();
            RSAPrivateKey privateKey = loadPrivateKey();
            return new KeyPair(publicKey, privateKey);
        } else {
            // Generate new key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Use 2048 bits for strong security
            return keyPairGenerator.generateKeyPair();
        }
    }

    /**
     * Loads the RSA public key from the configured resource.
     *
     * @return the RSA public key
     */
    private RSAPublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = publicKeyResource.getInputStream().readAllBytes();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * Loads the RSA private key from the configured resource.
     *
     * @return the RSA private key
     */
    private RSAPrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = privateKeyResource.getInputStream().readAllBytes();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * Generates a unique JWT ID for token identification.
     *
     * @return a unique JWT ID
     */
    @Bean
    public String jwtId() {
        return UUID.randomUUID().toString();
    }
}