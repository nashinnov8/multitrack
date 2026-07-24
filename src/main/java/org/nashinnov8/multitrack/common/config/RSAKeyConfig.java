package org.nashinnov8.multitrack.common.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.nashinnov8.multitrack.common.jwt.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSAKeyConfig {

  @Bean
  RSAPublicKey rsaPublicKey(JwtProperties jwtProperties) {
    return jwtProperties.publicKey();
  }

  @Bean
  RSAPrivateKey rsaPrivateKey(JwtProperties jwtProperties) {
    return jwtProperties.privateKey();
  }
}
