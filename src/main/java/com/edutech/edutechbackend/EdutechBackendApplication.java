package com.edutech.edutechbackend;

import com.edutech.edutechbackend.upskilling.service.GroqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GroqProperties.class)
public class EdutechBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(EdutechBackendApplication.class, args);
	}
}