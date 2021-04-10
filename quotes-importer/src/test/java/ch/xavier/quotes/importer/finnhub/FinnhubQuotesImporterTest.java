package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.Quote.QuoteType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
class FinnhubQuotesImporterTest {

    private FinnhubQuotesImporter importer;

    @BeforeEach
    public void setUp() {
        importer = new FinnhubQuotesImporter();
    }

    @Test
    public void getDailyValuesForSymbol_returnsCorrectData_givenAvailableAPI() {
        // GIVEN
        String testSymbol = "FB";

        // WHEN
        Long count = importer.getQuotes(Flux.just(testSymbol), QuoteType.DAILY).count().block();

        // THEN
        Assertions.assertEquals(1944, count);
    }

    @Test
    public void getFiveMinValuesForSymbol_returnsCorrectData_givenAvailableAPI() {
        // GIVEN
        String testSymbol = "FB";

        // WHEN
        Long count = importer.getQuotes(Flux.just(testSymbol), QuoteType.FIVE_MIN).count().block();

        // THEN
        Assertions.assertEquals(13923, count);
    }
}