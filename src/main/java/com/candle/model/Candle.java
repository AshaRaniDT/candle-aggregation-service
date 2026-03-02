package com.candle.model;

public record Candle(
		long time,
		double open,
		double high,
		double low,
		double close,
		long volume
) {

	public static Candle open(long time, double price) {
		return new Candle(time, price, price, price, price, 1L);
	}

	public Candle update(double price) {
		return new Candle(
				time,
				open,
				Math.max(high, price),
				Math.min(low, price),
				price,
				volume + 1
		);
	}

}
