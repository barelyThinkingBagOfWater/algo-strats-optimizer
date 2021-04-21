package ch.xavier.tradingbot.quote;

import ch.xavier.tradingbot.quote.typed.QuoteType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
//@TestPropertySource(properties = "mongodb.host=172.18.42.2")
@Slf4j
class MongoQuotesRepositoryTest {

    @Autowired
    private MongoQuotesRepository repository;


    @Test
    public void test_with_quotes() {
        // WHEN
        repository.findAllBySymbol("FB", QuoteType.FIVE_MIN)
                .count()
                .doOnNext(count -> log.info("Count of FB hourly quotes in db:{}", count))
                .block();
    }

    @Test
    public void test_with_quotes_since_date() {
        // WHEN
        repository.findAllBySymbolSinceDate("FB", QuoteType.FIVE_MIN,
                LocalDateTime.of(2020, 1, 1, 12, 00))
                .count()
                .doOnNext(count -> log.info("Count of FB hourly quotes in db since 1st January 2020:{}", count))
                .block();
    }

    @Test
    public void getAllSymbols_returnsAllSymbol() {
        // GIVEN

        // WHEN
        List<String> symbols = repository.getAllStoredSymbols(QuoteType.DAILY).collectList().block();

        // THEN
        log.info("Symbols:{}", symbols);
    }
}