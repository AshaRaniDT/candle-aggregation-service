package com.candle.aggregation;

public record CandleKey(String symbol, String interval, long bucketTime) {

		public static CandleKey of(String symbol, String interval, long eventTimestampSeconds) {
			long intervalSeconds = parseIntervalSeconds(interval);
			long bucket = (eventTimestampSeconds / intervalSeconds) * intervalSeconds;
			return new CandleKey(symbol, interval, bucket);
		}

		public static long parseIntervalSeconds(String interval) {
			if (interval == null || interval.isBlank()) {
				throw new IllegalArgumentException("Interval must not be blank");
			}
			char unit = interval.charAt(interval.length() - 1);
			int value = Integer.parseInt(interval.substring(0, interval.length() - 1));
			return switch (unit) {
				case 's' -> value;
				case 'm' -> value * 60L;
				case 'h' -> value * 3600L;
				case 'd' -> value * 86400L;
				default  -> throw new IllegalArgumentException("Unsupported interval unit: " + unit);
			};
		}
}

