# Candle Aggregation Service

A production-style Spring Boot microservice that ingests real-time bid/ask market data, aggregates it into OHLC candlesticks 
per symbol and interval, and exposes a TradingView-compatible REST API.

## Overview
This service simulates high-frequency market data and converts bid/ask ticks into time-aligned OHLC candles across multiple symbols and intervals. 
It is designed to demonstrate:
-Real-time aggregation
-Thread-safe state management
-Time-bucket alignment
-REST API design
-Observability via Spring Boot Actuator
-Clean unit & integration testing

## Architecture
```
MarketDataSimulator
        ↓
   CandleAggregator  (stateful, thread-safe)
        ↓
 InMemoryCandleStore (sorted, concurrent)
        ↓
     /history API
        ↓
 TradingView / Dashboard
```

## Key components:
- `MarketDataSimulator` — generates random walk bid/ask events at configurable rate
- `CandleAggregator` — stateful, thread-safe OHLC builder using ConcurrentHashMap
- `InMemoryCandleStore` — time-sorted ConcurrentSkipListMap per (symbol, interval)
- `HistoryController` — REST endpoint returning TradingView-compatible JSON

## Running the Application
- Requirements: Java 17+, Gradle 8+

```bash
./gradlew bootRun
```

## Query history:
```bash
curl "http://localhost:8080/history?symbol=BTC-USD&interval=1m&from=0&to=9999999999"
```
**Response format JSON (TradingView-compatible):**
{
"s": "ok",
"t": [1700000000, 1700000060],
"o": [29500.0, 29505.0],
"h": [29510.0, 29515.0],
"l": [29490.0, 29500.0],
"c": [29505.0, 29512.0],
"v": [10, 8]
}

**if no data exists in the range:**
{
"s": "no_data",
"t": [],
"o": [],
"h": [],
"l": [],
"c": [],
"v": []
}

**Health check:**
```bash
curl "http://localhost:8080/actuator/health"
```
**Response:**
{
"status": "UP",
"components": {
"aggregatorHealthIndicator": {
"status": "UP",
"details": {
"liveCandles": 5,
"status": "aggregating"
}
}
}
}

## Running Tests
```bash
./gradlew test
```
Test coverage includes:
-Candle domain model
-InMemoryCandleStore unit tests
-CandleAggregator rollover logic
-HistoryController integration tests
-Actuator health endpoint tests

## Configuration (`application.yml`)
| Property                             | Default                 | Description            
| `candle.symbols`                     | `[BTC-USD, ETH-USD]`    | Symbols to simulate    
| `candle.intervals`                   | `[1s, 5s, 1m, 15m, 1h]` | Aggregation timeframes 
| `candle.simulator.enabled`           | `true`                  | Enable simulator       
| `candle.simulator.events-per-second` | `10`                    | Tick rate per symbol   


## Design & Performance Trade-offs

- In-memory storage for simplicity and speed; no persistence across restarts. Easily replaceable via CandleStore interface.

- Persist-on-every-tick (upsert model) ensures live history visibility but increases write frequency.

- ConcurrentHashMap + ConcurrentSkipListMap provide thread safety and sorted time-range queries with minimal locking.

- Mid-price aggregation (bid + ask) / 2 used for simplicity; real trading systems would use executed trade price.

- No late-event handling; out-of-order events may fall into incorrect buckets (production systems require watermarking).

- Optimized for low-latency reads and moderate write throughput (~100+ updates/sec by default).


