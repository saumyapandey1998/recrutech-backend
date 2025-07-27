package com.recrutech.recrutechauth.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${jwt.public.key}")
    private String publicKeyPath;

    @Value("${jwt.private.key}")
    private String privateKeyPath;
    
    @Value("${jwt.private.key.passphrase:password}")
    private String privateKeyPassphrase;

    @Bean
    public RSAPublicKey publicKey() throws Exception {
        String pem = loadKey(publicKeyPath)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        try {
            // Load the encrypted key file
            String encryptedPem = loadKey(privateKeyPath);
            
            // Create a PEM parser to read the encrypted key
            PEMParser pemParser = new PEMParser(new StringReader(encryptedPem));
            
            // Parse the encrypted private key info
            Object parsedObject = pemParser.readObject();
            pemParser.close();
            
            // Convert to RSA private key based on the object type
            if (parsedObject == null) {
                throw new IllegalArgumentException("Failed to parse private key. Parsed object is null.");
            }
            
            // Use BouncyCastle to handle the encrypted key
            PrivateKey privateKey;
            
            if (encryptedPem.contains("ENCRYPTED PRIVATE KEY")) {
                // For PKCS#8 encrypted format (BEGIN ENCRYPTED PRIVATE KEY)
                // Since we're having issues with the BouncyCastle PKCS8 classes,
                // we'll use a more direct approach with Java's standard libraries
                
                // Extract the encrypted key data
                String cleanPem = encryptedPem
                        .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                        .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                byte[] encryptedData = Base64.getDecoder().decode(cleanPem);
                
                // Use Java's standard libraries with BouncyCastle as the provider
                javax.crypto.EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = 
                        new javax.crypto.EncryptedPrivateKeyInfo(encryptedData);
                
                // Create a PBE key spec using the passphrase
                javax.crypto.spec.PBEKeySpec pbeKeySpec = 
                        new javax.crypto.spec.PBEKeySpec(privateKeyPassphrase.toCharArray());
                
                // Get the algorithm used for encryption
                String algName = encryptedPrivateKeyInfo.getAlgName();
                
                // Create a secret key factory for the encryption algorithm
                javax.crypto.SecretKeyFactory keyFactory = 
                        javax.crypto.SecretKeyFactory.getInstance(algName);
            
                // Generate the secret key from the passphrase
                javax.crypto.SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
                
                // Decrypt the private key using the secret key
                PKCS8EncodedKeySpec pkcs8KeySpec = encryptedPrivateKeyInfo.getKeySpec(secretKey);
                
                // Create an RSA key factory
                KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
                
                // Generate the private key from the decrypted key spec
                privateKey = rsaKeyFactory.generatePrivate(pkcs8KeySpec);
            } else if (parsedObject instanceof PEMEncryptedKeyPair) {
                // For traditional PEM encrypted format (BEGIN RSA PRIVATE KEY with encryption)
                PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) parsedObject;
                
                // Decrypt the key pair
                PEMKeyPair keyPair = encryptedKeyPair.decryptKeyPair(
                        new JcePEMDecryptorProviderBuilder()
                                .build(privateKeyPassphrase.toCharArray())
                );
                
                // Convert to private key
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                privateKey = converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            } else {
                throw new IllegalArgumentException("Unsupported key format: " + parsedObject.getClass().getName());
            }
            
            // Ensure we have an RSA private key
            if (!(privateKey instanceof RSAPrivateKey)) {
                throw new IllegalArgumentException("The key is not an RSA private key");
            }
            
            return (RSAPrivateKey) privateKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load encrypted private key: " + e.getMessage(), e);
        }
    }

    private String loadKey(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String rp = path.substring("classpath:".length());
            try (var in = getClass().getClassLoader().getResourceAsStream(rp)) {
                if (in == null) throw new IOException("Key not found: " + rp);
                return new String(in.readAllBytes());
            }
        } else {
            return Files.readString(new File(path).toPath());
        }
    }
}