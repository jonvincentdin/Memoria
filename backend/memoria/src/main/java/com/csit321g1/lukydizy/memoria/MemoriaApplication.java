package com.csit321g1.lukydizy.memoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Turns on Spring Boot auto-configuration and scans this package for controllers, services and repositories.
@SpringBootApplication
public class MemoriaApplication {

	public static void main(String[] args) {
		// Starts the embedded web server (port 8080 by default).
		SpringApplication.run(MemoriaApplication.class, args);
	}

}