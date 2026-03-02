package com.candle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CandleAggregationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandleAggregationServiceApplication.class, args);
	}

}
