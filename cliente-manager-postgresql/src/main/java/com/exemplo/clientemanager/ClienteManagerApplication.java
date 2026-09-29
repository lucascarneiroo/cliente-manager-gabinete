package com.exemplo.clientemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClienteManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteManagerApplication.class, args);
    }

}
