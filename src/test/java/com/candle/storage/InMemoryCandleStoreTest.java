package com.candle.storage;

import com.candle.model.Candle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCandleStoreTest {

	private InMemoryCandleStore store;

	@BeforeEach
	void setUp() {
		store = new InMemoryCandleStore();
	}

	@Test
	void savesAndRetrievesSingleCandle() {
		Candle candle = new Candle(
				1000L, 100.0, 110.0, 90.0, 105.0, 10L
		);

		store.save("BTC-USD", "1m", candle);

		List<Candle> result =
				store.query("BTC-USD", "1m", 1000L, 1000L);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(candle);
	}

	@Test
	void queryReturnsOnlyCandlesWithinRange() {
		store.save("BTC-USD", "1m",
				new Candle(1000L, 100, 110, 90, 105, 10));
		store.save("BTC-USD", "1m",
				new Candle(1060L, 105, 115, 95, 110, 20));
		store.save("BTC-USD", "1m",
				new Candle(1120L, 110, 120, 100, 115, 30));

		List<Candle> result =
				store.query("BTC-USD", "1m", 1000L, 1060L);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).time()).isEqualTo(1000L);
		assertThat(result.get(1).time()).isEqualTo(1060L);
	}

	@Test
	void returnsEmptyListWhenNoData() {
		List<Candle> result =
				store.query("UNKNOWN", "1m", 0L, 1000L);

		assertThat(result).isEmpty();
	}

	@Test
	void maintainsSortedOrderByTimestamp() {
		store.save("BTC-USD", "1m",
				new Candle(1120L, 110, 120, 100, 115, 30));
		store.save("BTC-USD", "1m",
				new Candle(1000L, 100, 110, 90, 105, 10));
		store.save("BTC-USD", "1m",
				new Candle(1060L, 105, 115, 95, 110, 20));

		List<Candle> result =
				store.query("BTC-USD", "1m", 0L, 2000L);

		assertThat(result).hasSize(3);
		assertThat(result.get(0).time()).isEqualTo(1000L);
		assertThat(result.get(1).time()).isEqualTo(1060L);
		assertThat(result.get(2).time()).isEqualTo(1120L);
	}

	@Test
	void isolatesDifferentSymbolsAndIntervals() {
		store.save("BTC-USD", "1m",
				new Candle(1000L, 100, 110, 90, 105, 10));

		store.save("ETH-USD", "1m",
				new Candle(1000L, 200, 210, 190, 205, 15));

		store.save("BTC-USD", "5m",
				new Candle(1000L, 100, 110, 90, 105, 10));

		List<Candle> btc1m =
				store.query("BTC-USD", "1m", 0L, 2000L);

		List<Candle> eth1m =
				store.query("ETH-USD", "1m", 0L, 2000L);

		List<Candle> btc5m =
				store.query("BTC-USD", "5m", 0L, 2000L);

		assertThat(btc1m).hasSize(1);
		assertThat(eth1m).hasSize(1);
		assertThat(btc5m).hasSize(1);
	}
}
