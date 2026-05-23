package com.example.qlbds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QlbdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(QlbdsApplication.class, args);
	}

}
