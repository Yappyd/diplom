package com.yappyd.authservice.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.yappyd.authservice.component.RsaKeyProvider;
import com.yappyd.authservice.exception.RefreshTokenException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
    private static final String TOKEN_TYPE_CLAIM = "token_type";

    private final RsaKeyProvider keyProvider;
    private final String keyId;
    @Getter
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final JWSVerifier verifier;

    public JwtService(
            RsaKeyProvider keyProvider,
            @Value("${jwt.key-id}") String keyId,
            @Value("${jwt.ttl.access-token}") Duration accessTtl,
            @Value("${jwt.ttl.refresh-token}") Duration refreshTtl
    ) {
        this.keyProvider = keyProvider;
        this.keyId = keyId;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.verifier = new RSASSAVerifier(keyProvider.getPublicKey());
    }

    public String generateAccessToken(UUID userId, Instant now) {
        return generateToken(userId, now, accessTtl, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(UUID userId, Instant now) {
        return generateToken(userId, now, refreshTtl, REFRESH_TOKEN_TYPE);
    }

    public UUID parseRefreshToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(verifier)) {
                throw new RefreshTokenException("Invalid token signature");
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                throw new RefreshTokenException("Refresh token expired");
            }

            String tokenType = claims.getStringClaim(TOKEN_TYPE_CLAIM);
            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
                throw new RefreshTokenException("Invalid token type: " + tokenType);
            }

            String subject = claims.getSubject();
            if (subject == null) {
                throw new RefreshTokenException("Missing subject claim");
            }

            return UUID.fromString(subject);

        } catch (IllegalArgumentException e) {
            throw new RefreshTokenException("Invalid subject format", e);
        } catch (ParseException e) {
            throw new RefreshTokenException("Malformed JWT", e);
        } catch (JOSEException e) {
            throw new RefreshTokenException("JWT verification failed", e);
        }
    }

    private String generateToken(UUID userId, Instant issuedAt, Duration ttl, String tokenType) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(issuedAt.plus(ttl)))
                    .claim(TOKEN_TYPE_CLAIM, tokenType)
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(keyId)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(keyProvider.getPrivateKey()));

            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}