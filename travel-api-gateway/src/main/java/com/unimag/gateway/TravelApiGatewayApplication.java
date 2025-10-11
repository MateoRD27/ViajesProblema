package com.unimag.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TravelApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelApiGatewayApplication.class, args);
	}

}
