package com.candle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "candle")
public record CandleAppProperties(List<String> symbols, List<String> intervals,
                                  Simulator simulator, Aggregation aggregation) {
	public record Simulator(boolean enabled, long eventsPerSecond) {
	}

	public record Aggregation(long flushRateMs, String lateEventPolicy) {
	}
}


