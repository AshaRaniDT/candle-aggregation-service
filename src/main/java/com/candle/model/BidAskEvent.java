package com.candle.model;

public record BidAskEvent(
		String symbol,
		double bid,
		double ask,
		long timestamp
) {
	public double midPrice() {
		return (bid + ask) / 2.0;
	}

	public long timestampSeconds() {
		return timestamp / 1000;
	}
}

