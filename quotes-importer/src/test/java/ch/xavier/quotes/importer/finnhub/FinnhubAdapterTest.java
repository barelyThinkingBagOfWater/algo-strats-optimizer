package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.Quote.QuoteType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
class FinnhubAdapterTest {

    private FinnhubAdapter adapter;

    @BeforeEach
    public void setUp() {
        adapter = new FinnhubAdapter();
    }

    @Test
    public void getDailyValuesForSymbol_returnsCorrectData_givenAvailableAPI() {
        // GIVEN
        String testSymbol = "FB";

        // WHEN
        Long count = adapter.getQuotes(Flux.just(testSymbol), QuoteType.DAILY).count().block();

        // THEN
        Assertions.assertEquals(1944, count);
    }

    @Test
    public void getFiveMinValuesForSymbol_returnsCorrectData_givenAvailableAPI() {
        // GIVEN
        String testSymbol = "FB";

        // WHEN
        Long count = adapter.getQuotes(Flux.just(testSymbol), QuoteType.FIVE_MIN).count().block();

        // THEN
        Assertions.assertEquals(13923, count);
    }
}