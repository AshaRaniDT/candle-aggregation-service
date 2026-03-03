package com.candle.health;

import com.candle.aggregation.CandleAggregator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AggregatorHealthIndicatorUnitTest {

	@Test
	void returnsAggregatingStatus() {
		CandleAggregator aggregator = Mockito.mock(CandleAggregator.class);
		when(aggregator.liveCount()).thenReturn(3);

		AggregatorHealthIndicator indicator =
				new AggregatorHealthIndicator(aggregator);

		Health health = indicator.health();

		assertThat(health.getStatus().getCode()).isEqualTo("UP");
		assertThat(health.getDetails())
				.containsEntry("liveCandles", 3)
				.containsEntry("status", "aggregating");
	}
}