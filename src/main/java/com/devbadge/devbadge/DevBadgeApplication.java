package com.devbadge.devbadge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})

public class DevBadgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevBadgeApplication.class, args);
    }

}
