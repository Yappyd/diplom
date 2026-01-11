package com.yappyd.authservice.component;

import com.yappyd.authservice.exception.KeyInitializationException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
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
            @Value("${jwt.keys.private}") String privateKeyPath,
            @Value("${jwt.keys.public}") String publicKeyPath
    ) {
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    private RSAPrivateKey loadPrivateKey(String path) {
        try {
            String pem = Files.readString(Path.of(path));
            return PEM_DECODER.decode(pem, RSAPrivateKey.class);
        } catch (Exception e) {
            throw new KeyInitializationException("Failed to load private key from path: " + path, e);
        }
    }

    private RSAPublicKey loadPublicKey(String path) {
        try {
            String pem = Files.readString(Path.of(path));
            return PEM_DECODER.decode(pem, RSAPublicKey.class);
        } catch (Exception e) {
            throw new KeyInitializationException("Failed to load public key from path: " + path, e);
        }
    }
}