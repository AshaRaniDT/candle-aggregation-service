package com.candle.api;

import com.candle.model.Candle;
import com.candle.storage.CandleStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
@Slf4j
public class HistoryController {

	private final CandleStore store;

	public HistoryController(CandleStore store) {
		this.store = store;
	}

	@GetMapping
	public ResponseEntity<HistoryResponse> getHistory(
			@RequestParam String symbol,
			@RequestParam String interval,
			@RequestParam long from,
			@RequestParam long to) {

		log.debug("History request: symbol={} interval={} from={} to={}",
				symbol, interval, from, to);

		if (from > to)
			return ResponseEntity.badRequest().body(HistoryResponse.noData());

		List<Candle> candles = store.query(symbol, interval, from, to);
		HistoryResponse response = HistoryResponse.from(candles);

		log.debug("Returning {} candles for {}/{}", candles.size(), symbol, interval);
		return ResponseEntity.ok(response);
	}
}