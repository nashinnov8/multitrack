package org.nashinnov8.multitrack.common.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    Long accessTokenExpirationSeconds,
    Long refreshTokenExpirationSeconds) {}
