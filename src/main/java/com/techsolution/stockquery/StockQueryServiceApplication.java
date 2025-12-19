package com.techsolution.stockquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StockQueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockQueryServiceApplication.class, args);
	}

}

