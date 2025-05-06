package com.moto.backmoto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.moto")
@EnableJpaRepositories(basePackages = "com.moto.repositorios")
@EntityScan(basePackages = {"com.moto.modelos", "com.moto.repositorios", "com.moto.controladores", "com.moto.dtos", "com.moto.backmoto"})
public class BackmotoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackmotoApplication.class, args);
	}

}
