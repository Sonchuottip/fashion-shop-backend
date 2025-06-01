package com.example.fashionshopbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FashionShopBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FashionShopBackendApplication.class, args);
    }

}
