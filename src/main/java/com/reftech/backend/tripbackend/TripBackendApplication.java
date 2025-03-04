package com.reftech.backend.tripbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class TripBackendApplication
{
    public static void main(String[] args) {
        SpringApplication.run(TripBackendApplication.class, args);
    }
}
