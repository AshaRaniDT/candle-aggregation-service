package com.candle.health;

import com.candle.aggregation.CandleAggregator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AggregatorHealthIndicator implements HealthIndicator {

	private final CandleAggregator aggregator;

	public AggregatorHealthIndicator(CandleAggregator aggregator) {
		this.aggregator = aggregator;
	}

	@Override
	public Health health() {
		int live = aggregator.liveCount();
		return Health.up()
				.withDetail("liveCandles", live)
				.withDetail("status", live > 0 ? "aggregating" : "idle")
				.build();
	}
}