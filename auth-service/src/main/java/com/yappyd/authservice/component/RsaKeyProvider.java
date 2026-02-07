package com.yappyd.authservice.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PEMDecoder;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class RsaKeyProvider {

    private static final PEMDecoder PEM_DECODER = PEMDecoder.of();

    @Getter
    private final RSAPrivateKey privateKey;
    @Getter
    private final RSAPublicKey publicKey;

    public RsaKeyProvider(
            @Value("${jwt.keys.private}") String privateKey,
            @Value("${jwt.keys.public}") String publicKey
    ) {
        this.privateKey = PEM_DECODER.decode(privateKey, RSAPrivateKey.class);
        this.publicKey = PEM_DECODER.decode(publicKey, RSAPublicKey.class);
    }
}