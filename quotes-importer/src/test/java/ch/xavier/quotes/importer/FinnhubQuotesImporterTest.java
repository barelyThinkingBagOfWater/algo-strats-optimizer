package ch.xavier.quotes.importer;

import ch.xavier.quotes.MongoQuotesRepository;
import ch.xavier.quotes.Quote.QuoteType;
import ch.xavier.quotes.importer.finnhub.FinnhubAdapter;
import ch.xavier.quotes.typedQuotes.DailyQuote;
import ch.xavier.quotes.typedQuotes.OneMinQuote;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

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
        String symbol = "BINANCE:BTCUSDT";

        repository.deleteAllQuotesOfTypeForSymbol(OneMinQuote.class, symbol)
                .thenMany(
                    importer.fetchAndSaveQuotes(Flux.just(symbol), QuoteType.ONE_MIN))
                .blockLast();
    }

    @Test
    public void save_all_crypto_symbols_on_binance() {
        List<String> symbols = adapter.getCryptoSymbolsForBinance()
                .collectList()
                .block();

        for (String symbol : symbols) {
            if (repository.findAllBySymbol(symbol, OneMinQuote.class).count().block() == 0) {
                log.info("Now saving one min quote for symbol:{}", symbol);
                importer.fetchAndSaveQuotes(symbol, QuoteType.ONE_MIN).blockLast();
            } else {
                log.info("Quotes already imported for symbol:{}", symbol);
            }
        }
    }

    @Test
    public void list_all_crypto_symbols_on_binance() {
        adapter.getCryptoSymbolsForBinance()
                .filter(symbol -> symbol.endsWith("USD"))
                .doOnNext(symbol -> log.info("Symbol:{}", symbol))
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