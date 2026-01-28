package com.yappyd.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.yappyd.authservice.component.RsaKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwksController {
    private final RsaKeyProvider keyProvider;
    private final String keyId;

    public JwksController(
            RsaKeyProvider keyProvider,
            @Value("${jwt.key-id}") String keyId
    ) {
        this.keyProvider = keyProvider;
        this.keyId = keyId;
    }


    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = keyProvider.getPublicKey();

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyID(keyId)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}