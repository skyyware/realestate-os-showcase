package com.skyyware.realestate.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.skyyware.realestate.config.RealEstateProperties;
import com.skyyware.realestate.identity.AppUser;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final byte[] secret;

    public JwtService(RealEstateProperties properties) {
        this.secret = properties.security().jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("realestate.security.jwt-secret must be at least 32 bytes");
        }
    }

    public String issue(AppUser user) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.id().toString())
                    .issuer("realestate-os")
                    .claim("email", user.email())
                    .claim("name", user.fullName())
                    .claim("role", user.role().name())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(60 * 60 * 12)))
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            JWSSigner signer = new MACSigner(secret);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not issue JWT", exception);
        }
    }

    public Optional<JwtPrincipal> verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);
            if (!jwt.verify(verifier)) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                return Optional.empty();
            }
            return Optional.of(new JwtPrincipal(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim("email"),
                    claims.getStringClaim("name"),
                    claims.getStringClaim("role")
            ));
        } catch (JOSEException | ParseException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
