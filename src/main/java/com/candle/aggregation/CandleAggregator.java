package com.candle.aggregation;

import com.candle.config.CandleAppProperties;
import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.storage.CandleStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CandleAggregator {

	private final CandleStore store;
	private final List<String> intervals;

	// (symbol + interval) -> active candle
	private final Map<Key, Candle> activeCandles = new ConcurrentHashMap<>();

	public CandleAggregator(CandleStore store, CandleAppProperties properties) {
		this.store = store;
		this.intervals = properties.intervals();
	}

	public void process(BidAskEvent event) {

		double price = (event.bid() + event.ask()) / 2.0;
		long epochSeconds = event.timestamp() / 1000;

		for (String interval : intervals) {

			long intervalSeconds = parseInterval(interval);
			long bucketStart = alignToBucket(epochSeconds, intervalSeconds);

			Key key = new Key(event.symbol(), interval);

			activeCandles.compute(key, (k, existing) -> {

				Candle next;

				if (existing == null) {
					next = newCandle(bucketStart, price);
				}
				else if (existing.time() == bucketStart) {
					next = updateCandle(existing, price);
				}
				else {
					// Bucket changed, persist old
					store.save(event.symbol(), interval, existing);
					next = newCandle(bucketStart, price);
				}

				store.save(event.symbol(), interval, next);

				return next;
			});
		}
	}

	private Candle newCandle(long bucketStart, double price) {
		return new Candle(bucketStart, price, price, price, price, 1);
	}

	private Candle updateCandle(Candle existing, double price) {
		return new Candle(
				existing.time(),
				existing.open(),
				Math.max(existing.high(), price),
				Math.min(existing.low(), price),
				price,
				existing.volume() + 1
		);
	}

	private long alignToBucket(long epochSeconds, long intervalSeconds) {
		return (epochSeconds / intervalSeconds) * intervalSeconds;
	}

	private long parseInterval(String interval) {
		if (interval.endsWith("s")) {
			return Long.parseLong(interval.replace("s", ""));
		}
		if (interval.endsWith("m")) {
			return Long.parseLong(interval.replace("m", "")) * 60;
		}
		if (interval.endsWith("h")) {
			return Long.parseLong(interval.replace("h", "")) * 3600;
		}
		throw new IllegalArgumentException("Unsupported interval: " + interval);
	}

	public int liveCount() {
		return activeCandles.size();
	}

	private record Key(String symbol, String interval) {
	}
}