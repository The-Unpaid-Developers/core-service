package com.project.core_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class CoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner logEndpoints(RequestMappingHandlerMapping mapping) {
		return args -> {
			mapping.getHandlerMethods().forEach((key, value) -> log.debug("Mapped: {}", key));
		};
	}
}
