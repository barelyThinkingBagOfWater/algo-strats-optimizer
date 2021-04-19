package ch.xavier.backtester.quote.mongo;

import ch.xavier.backtester.quote.MongoQuotesRepository;
import ch.xavier.backtester.quote.typed.QuoteType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        repository.findAllBySymbol("FB", QuoteType.HOURLY)
                .count()
                .doOnNext(count -> log.info("Count of FB hourly quotes in db:{}", count))
                .block();
    }

//    @Test
    public void getAllSymbols_returnsAllSymbol() {
        // GIVEN

        // WHEN
        List<String> symbols = repository.getAllStoredSymbols().collectList().block();

        // THEN
        log.info("Symbols:{}", symbols);
    }
}