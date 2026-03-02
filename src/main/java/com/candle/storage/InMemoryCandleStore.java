package com.candle.storage;

import com.candle.model.Candle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class InMemoryCandleStore implements CandleStore {

	private static final Logger log = LoggerFactory.getLogger(InMemoryCandleStore.class);

	// (symbol|interval) -> (bucketTime -> Candle)
	private final ConcurrentMap<String, NavigableMap<Long, Candle>> store = new ConcurrentHashMap<>();

	private static String key(String symbol, String interval) {
		return symbol + "|" + interval;
	}

	@Override
	public void save(String symbol, String interval, Candle candle) {
		store.computeIfAbsent(key(symbol, interval), k -> new ConcurrentSkipListMap<>())
				.put(candle.time(), candle);

		if (log.isDebugEnabled()) {
			log.debug("Saved candle [{} {} {}] O={} H={} L={} C={} V={}",
					symbol, interval, candle.time(),
					candle.open(), candle.high(), candle.low(), candle.close(), candle.volume());
		}
	}

	@Override
	public List<Candle> query(String symbol, String interval, long fromSeconds, long toSeconds) {
		var series = store.get(key(symbol, interval));
		if (series == null) return List.of();

		return new ArrayList<>(series.subMap(fromSeconds, true, toSeconds, true).values());
	}
}