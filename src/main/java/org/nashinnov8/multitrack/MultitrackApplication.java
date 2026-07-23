package org.nashinnov8.multitrack;

import org.nashinnov8.multitrack.common.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class MultitrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultitrackApplication.class, args);
    }

}
