package com.candle.api;

import com.candle.model.Candle;
import com.candle.storage.CandleStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		properties = "app.simulator.enabled=false"
)
@AutoConfigureMockMvc
public class HistoryControllerTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	CandleStore store;

	@Test
	void returnsOkWithCandles() throws Exception {

		when(store.query(eq("BTC-USD"), eq("1m"), eq(1620000000L), eq(1620000060L)))
				.thenReturn(List.of(
						new Candle(1620000000L, 29500.0, 29510.0, 29490.0, 29505.0, 10L)
				));

		mvc.perform(get("/history")
						.param("symbol", "BTC-USD")
						.param("interval", "1m")
						.param("from", "1620000000")
						.param("to",   "1620000060"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.s").value("ok"))
				.andExpect(jsonPath("$.t[0]").value(1620000000))
				.andExpect(jsonPath("$.o[0]").value(29500.0))
				.andExpect(jsonPath("$.v[0]").value(10));
	}

	@Test
	void returnsNoDataWhenEmpty() throws Exception {
		mvc.perform(get("/history")
						.param("symbol", "UNKNOWN")
						.param("interval", "1m")
						.param("from", "0")
						.param("to",   "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.s").value("no_data"));
	}

	@Test
	void returnsBadRequestWhenFromAfterTo() throws Exception {
		mvc.perform(get("/history")
						.param("symbol", "BTC-USD")
						.param("interval", "1m")
						.param("from", "9999")
						.param("to",   "1000"))
				.andExpect(status().isBadRequest());
	}
}