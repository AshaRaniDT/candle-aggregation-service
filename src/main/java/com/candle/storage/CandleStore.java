package com.candle.storage;

import com.candle.model.Candle;

import java.util.List;

public interface CandleStore {

	void save(String symbol, String interval, Candle candle);

	List<Candle> query(String symbol, String interval, long fromSeconds, long toSeconds);
}
