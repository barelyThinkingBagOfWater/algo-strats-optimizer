package ch.xavier.quotes.importer;

import ch.xavier.quotes.MongoQuotesRepository;
import ch.xavier.quotes.Quote.QuoteType;
import ch.xavier.quotes.importer.finnhub.FinnhubAdapter;
import ch.xavier.quotes.typedQuotes.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
//@TestPropertySource(properties = "mongodb.host=172.18.42.2")
@Slf4j
class FinnhubQuotesImporterTest {

    @Autowired
    private FinnhubAdapter adapter;
    @Autowired
    private MongoQuotesRepository repository;
    @Autowired
    private FinnhubQuotesImporter importer;


    @Test
    public void populateDB_with_daily_quotes_of_FB() {
        repository.deleteAllQuotesOfTypeForSymbol(DailyQuote.class, "FB").thenMany(
                importer.fetchAndSaveQuotes(Flux.just("FB"), QuoteType.DAILY))
                .blockLast();
    }

    @Test
    public void populateDB_with_daily_quotes_of_random_crypto() {
        repository.deleteAllQuotesOfTypeForSymbol(DailyQuote.class, "BINANCE:LINABTC").thenMany(
                importer.fetchAndSaveQuotes(Flux.just("BINANCE:LINABTC"), QuoteType.DAILY))
                .blockLast();
    }

    @Test //Takes ~90 seconds
    public void populateDB_with_all_quotes_of_FB() {
        importer.fetchAndSaveQuotesOfAllTypes(Flux.just("FB")).blockLast();
    }

    @Test //Should take ~2h03min (82 * 90)
    public void populateDB_with_all_quotes_of_common_us_symbols() {
        importer.fetchAndSaveQuotesOfAllTypes(SymbolsRegistry.MOST_TRADED_US_SYMBOLS).blockLast();
    }

    @Test
    public void populateDB_with_hourly_quotes_of_common_us_symbols() {
        repository.deleteAllQuotesOfType(OneMinQuote.class).block();

        importer.fetchAndSaveQuotes(SymbolsRegistry.MOST_TRADED_US_SYMBOLS, QuoteType.ONE_MIN).blockLast();
    }
}