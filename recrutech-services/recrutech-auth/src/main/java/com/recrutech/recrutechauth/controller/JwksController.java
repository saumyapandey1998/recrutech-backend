package com.recrutech.recrutechauth.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Controller for exposing JWK Set endpoint.
 * This endpoint is used by OAuth2 Resource Servers to validate JWTs.
 */
@RestController
@RequestMapping("/api/oauth2")
public class JwksController {

    private final KeyPair keyPair;
    private final String jwtId;

    /**
     * Constructor for JwksController.
     *
     * @param keyPair the RSA key pair for JWT signing and verification
     * @param jwtId the JWT ID for token identification
     */
    public JwksController(KeyPair keyPair, String jwtId) {
        this.keyPair = keyPair;
        this.jwtId = jwtId;
    }

    /**
     * Exposes the JWK Set endpoint.
     * Returns the public key information needed to validate JWTs.
     *
     * @return the JWK Set as a Map
     */
    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        // Create an RSA key with the public key and specify the algorithm
        JWK jwk = new RSAKey.Builder((RSAPublicKey) this.keyPair.getPublic())
                .algorithm(JWSAlgorithm.RS256)
                .keyID(this.jwtId)
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .build();
        
        // Create a JWK Set with the JWK
        JWKSet jwkSet = new JWKSet(jwk);
        
        // Return the JWK Set as JSON
        return jwkSet.toJSONObject();
    }
}