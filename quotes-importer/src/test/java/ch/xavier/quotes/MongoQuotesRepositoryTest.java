package ch.xavier.quotes;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

@SpringBootTest
@Slf4j
//@TestPropertySource(properties = "mongodb.host=172.18.42.2")
class MongoQuotesRepositoryTest {

    @Autowired
    private MongoQuotesRepository repository;


    @BeforeEach
    void setUp() {
        repository.createUniqueIndexOnSymbolAndTimestamp(ITestQuote.class).block();
        repository.deleteAllQuotesOfType(ITestQuote.class).block();
    }


    @Test
    public void uniqueness_of_quotes_is_respected_in_Db() {
        // GIVEN
        String symbol = "SYMBOL";
        Quote quoteToSave1 = new ITestQuote(symbol, 1L, 2.0, 3.0, 4.0, 1.0, 2L);
        Quote quoteToSave2 = new ITestQuote(symbol, 1L, 2.0, 3.0, 4.0, 1.0, 2L);
        Quote quoteToSave3 = new ITestQuote(symbol, 3L, 2.0, 3.0, 4.0, 1.0, 2L);

        Flux.just(quoteToSave1, quoteToSave2, quoteToSave3)
                .flatMap(repository::save)

                // WHEN
                .thenMany(repository.findAllBySymbol(symbol, ITestQuote.class))

                // THEN
                .doOnNext(Assertions::assertNotNull)
                .doOnNext(quote -> Assertions.assertEquals(quote.getClose(), quoteToSave1.getClose()))
                .doOnNext(quote -> Assertions.assertEquals(quote.getHigh(), quoteToSave1.getHigh()))
                .doOnNext(quote -> Assertions.assertEquals(quote.getLow(), quoteToSave1.getLow()))
                .doOnNext(quote -> Assertions.assertEquals(quote.getOpen(), quoteToSave1.getOpen()))
                .doOnNext(quote -> Assertions.assertEquals(quote.getVolume(), quoteToSave1.getVolume()))
                .doOnNext(quote -> log.info("Quote considered in test with 3 quotes:{}", quote))
                .then(repository.count(ITestQuote.class))
                .doOnNext(count -> Assertions.assertEquals(2, count))
                .block();
    }
}