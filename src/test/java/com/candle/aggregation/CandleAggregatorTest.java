package com.candle.aggregation;

import com.candle.config.CandleAppProperties;
import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.storage.InMemoryCandleStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class CandleAggregatorTest {

	private InMemoryCandleStore store;
	private CandleAggregator aggregator;

	@BeforeEach
	void setUp() {
		store = new InMemoryCandleStore();

		var props = new CandleAppProperties(
				List.of("BTC-USD", "ETH-USD"),          // symbols
				List.of("1s", "1m"),                    // intervals
				new CandleAppProperties.Simulator(false, 100),
				new CandleAppProperties.Aggregation(1000, "IGNORE")
		);

		aggregator = new CandleAggregator(store, props);
	}

	@Test
	void firstEventOpensCandle() {
		long ts = 1_700_000_000_000L; // millis
		aggregator.process(new BidAskEvent("BTC-USD", 100.0, 100.1, ts));

		List<Candle> candles = store.query("BTC-USD", "1s",
				ts / 1000 - 1, ts / 1000 + 1);

		assertThat(candles).hasSize(1);
		Candle c = candles.get(0);
		assertThat(c.open()).isEqualTo(100.05); // mid-price
		assertThat(c.volume()).isEqualTo(1L);
	}

	@Test
	void multipleEventsInSameBucketAggregateCorrectly() {
		long base = 1_700_000_000_000L;
		aggregator.process(new BidAskEvent("ETH-USD", 200.0, 200.2, base));
		aggregator.process(new BidAskEvent("ETH-USD", 210.0, 210.2, base + 100)); // +100ms, same 1s bucket
		aggregator.process(new BidAskEvent("ETH-USD", 190.0, 190.2, base + 200));

		List<Candle> candles = store.query("ETH-USD", "1s",
				base / 1000, base / 1000);

		assertThat(candles).hasSize(1);
		Candle c = candles.get(0);
		assertThat(c.high()).isGreaterThan(210.0);
		assertThat(c.low()).isLessThan(191.0);
		assertThat(c.volume()).isEqualTo(3L);
	}

	@Test
	void eventsInDifferentBucketsCreateSeparateCandles() {
		aggregator.process(new BidAskEvent("BTC-USD", 100.0, 100.2, 1_700_000_000_000L));
		aggregator.process(new BidAskEvent("BTC-USD", 200.0, 200.2, 1_700_000_005_000L)); // +5 seconds

		List<Candle> candles = store.query("BTC-USD", "1s",
				1_700_000_000L, 1_700_000_010L);

		assertThat(candles).hasSizeGreaterThanOrEqualTo(2);
	}
}