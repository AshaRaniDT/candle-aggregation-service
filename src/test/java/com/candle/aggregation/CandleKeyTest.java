package com.candle.aggregation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CandleKeyTest {

	@Test
	void bucketsAlignToIntervalBoundary() {
		// 1703 seconds since epoch → 1m bucket should be 1680 (floor to 60)
		long ts = 1703L;
		CandleKey key = CandleKey.of("BTC-USD", "1m", ts);
		assertThat(key.bucketTime()).isEqualTo(1680L);
	}

	@Test
	void parseSeconds() {
		assertThat(CandleKey.parseIntervalSeconds("1s")).isEqualTo(1);
		assertThat(CandleKey.parseIntervalSeconds("5s")).isEqualTo(5);
		assertThat(CandleKey.parseIntervalSeconds("1m")).isEqualTo(60);
		assertThat(CandleKey.parseIntervalSeconds("15m")).isEqualTo(900);
		assertThat(CandleKey.parseIntervalSeconds("1h")).isEqualTo(3600);
	}

	@Test
	void invalidUnitThrows() {
		assertThatThrownBy(() -> CandleKey.parseIntervalSeconds("1x"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}