package com.skyyware.realestate;

import com.skyyware.realestate.config.RealEstateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RealEstateProperties.class)
public class RealestateApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealestateApiApplication.class, args);
	}

}
