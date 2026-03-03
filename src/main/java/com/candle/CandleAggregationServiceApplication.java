package com.candle;

import com.candle.config.CandleAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(CandleAppProperties.class)
public class CandleAggregationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandleAggregationServiceApplication.class, args);
	}

}
