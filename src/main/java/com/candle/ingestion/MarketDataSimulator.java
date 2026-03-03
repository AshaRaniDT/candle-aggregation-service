package com.candle.ingestion;

import com.candle.aggregation.CandleAggregator;
import com.candle.config.CandleAppProperties;
import com.candle.model.BidAskEvent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
		prefix = "candle.simulator",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true
)
@Slf4j
public class MarketDataSimulator {

	private final CandleAggregator aggregator;
	private final List<String> symbols;
	private final int eventsPerSecond;
	private final ScheduledExecutorService scheduler;
	private final Random random = new Random();

	private static final Map<String, Double> BASE_PRICES = Map.of(
			"BTC-USD", 65000.0,
			"ETH-USD", 3500.0
	);

	public MarketDataSimulator(
			CandleAggregator aggregator,
			CandleAppProperties properties) {

		this.aggregator = aggregator;
		this.symbols = properties.symbols();
		this.eventsPerSecond = Math.toIntExact(properties.simulator().eventsPerSecond());

		this.scheduler = Executors.newScheduledThreadPool(
				symbols.size(),
				r -> {
					Thread t = new Thread(r, "market-sim");
					t.setDaemon(true);
					return t;
				}
		);

		start();
	}

	private void start() {
		long delayMicros = 1_000_000L / eventsPerSecond;

		for (String symbol : symbols) {
			double[] price = { BASE_PRICES.getOrDefault(symbol, 100.0) };

			scheduler.scheduleAtFixedRate(() -> {
				try {
					double change = (random.nextDouble() - 0.5) * 0.001 * price[0];
					price[0] = Math.max(0.01, price[0] + change);

					double spread = price[0] * 0.0002;
					double bid = price[0] - spread / 2;
					double ask = price[0] + spread / 2;

					BidAskEvent event = new BidAskEvent(
							symbol, bid, ask, System.currentTimeMillis()
					);

					aggregator.process(event);

				} catch (Exception e) {
					log.error("Simulator error for {}: {}", symbol, e.getMessage());
				}
			}, 0, delayMicros, TimeUnit.MICROSECONDS);
		}

		log.info("Market simulator started: {} symbols @ {} events/sec",
				symbols.size(), eventsPerSecond);
	}

	@PreDestroy
	public void stop() {
		scheduler.shutdown();
		log.info("Market simulator stopped");
	}
}