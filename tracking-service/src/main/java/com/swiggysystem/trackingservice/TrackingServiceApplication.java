package com.swiggysystem.trackingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class TrackingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackingServiceApplication.class, args);
	}

}
