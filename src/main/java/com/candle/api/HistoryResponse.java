package com.candle.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.candle.model.Candle;
import java.util.List;

public record HistoryResponse(
		@JsonProperty("s") String status,
		@JsonProperty("t") List<Long> times,
		@JsonProperty("o") List<Double> opens,
		@JsonProperty("h") List<Double> highs,
		@JsonProperty("l") List<Double> lows,
		@JsonProperty("c") List<Double> closes,
		@JsonProperty("v") List<Long> volumes
) {
	public static HistoryResponse noData() {
		return new HistoryResponse("no_data",
				List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
	}

	public static HistoryResponse from(List<Candle> candles) {
		if (candles.isEmpty()) return noData();

		List<Long>   t = candles.stream().map(Candle::time).toList();
		List<Double> o = candles.stream().map(Candle::open).toList();
		List<Double> h = candles.stream().map(Candle::high).toList();
		List<Double> l = candles.stream().map(Candle::low).toList();
		List<Double> c = candles.stream().map(Candle::close).toList();
		List<Long>   v = candles.stream().map(Candle::volume).toList();

		return new HistoryResponse("ok", t, o, h, l, c, v);
	}
}