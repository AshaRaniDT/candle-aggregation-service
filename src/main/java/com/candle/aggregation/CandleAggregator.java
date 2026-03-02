package com.candle.aggregation;

import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.storage.CandleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core aggregation engine.
 * Processes BidAskEvents and maintains live (open) candle state per
 * (symbol, interval) pair. On bucket close, flushes to CandleStore.
 * Thread-safety: ConcurrentHashMap + atomic compute operations ensure
 * no data races under concurrent event delivery.
 */

@Component
public class CandleAggregator {

	private static final Logger log = LoggerFactory.getLogger(CandleAggregator.class);

	private final CandleStore store;
	private final List<String> intervals;
	
	private final Map<CandleKey, Candle> liveCandles = new ConcurrentHashMap<>();

	public CandleAggregator(CandleStore store,
	                        org.springframework.boot.context.properties.bind.Binder binder) {
		this.store = store;
		// Read configured intervals from application.yml
		this.intervals = binder
				.bind("candle.intervals", String[].class)
				.map(List::of)
				.orElse(List.of("1s", "1m", "1h"));
	}

	// Process a single market event. Called concurrently from event publisher
	public void process(BidAskEvent event) {
		log.trace("Processing event: {} bid={} ask={} ts={}",
				event.symbol(), event.bid(), event.ask(), event.timestamp());

		double price = event.midPrice();
		long tsSeconds = event.timestampSeconds();

		for (String interval : intervals) {
			CandleKey key = CandleKey.of(event.symbol(), interval, tsSeconds);

			liveCandles.compute(key, (k, existing) -> {
				if (existing == null) {
					log.debug("Opening new candle [{} {} {}]", k.symbol(), k.interval(), k.bucketTime());
					return Candle.open(k.bucketTime(), price);
				}
				return existing.update(price);
			});

			// Persist the updated live candle immediately (upsert semantics)
			Candle current = liveCandles.get(key);
			if (current != null) {
				store.save(event.symbol(), interval, current);
			}
		}
	}

	// Flush all live candles
	public void flushAll() {
		liveCandles.forEach((key, candle) ->
				store.save(key.symbol(), key.interval(), candle));
		log.info("Flushed {} live candles to store", liveCandles.size());
	}

	public int liveCount() {
		return liveCandles.size();
	}
}