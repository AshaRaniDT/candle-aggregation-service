package com.candle.ingestion;

import com.candle.model.Candle;
import com.candle.storage.CandleStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class HistoricalDataSeeder implements ApplicationRunner {

	private final CandleStore store;

	@Override
	public void run(org.springframework.boot.ApplicationArguments args) {

		log.info("Seeding historical candles...");

		String symbol = "BTC-USD";
		String interval = "1m";

		long startEpoch = 1620000000L;   // May 2021
		long minutesToSeed = 120;        // 2 hours of data

		double price = 29500.0;
		Random random = new Random();

		for (int i = 0; i < minutesToSeed; i++) {

			long timestamp = startEpoch + (i * 60);

			double open = price;
			double high = open + random.nextDouble() * 20;
			double low = open - random.nextDouble() * 20;
			double close = low + random.nextDouble() * (high - low);
			long volume = 5 + random.nextInt(20);

			Candle candle = new Candle(
					timestamp,
					open,
					high,
					low,
					close,
					volume
			);

			store.save(symbol, interval, candle);

			price = close;
		}

		log.info("Seeded {} historical candles for {}", minutesToSeed, symbol);
	}
}