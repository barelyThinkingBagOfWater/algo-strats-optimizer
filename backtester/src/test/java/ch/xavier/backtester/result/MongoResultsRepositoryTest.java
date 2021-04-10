package ch.xavier.backtester.result;

import ch.xavier.backtester.quote.typed.QuoteType;
import ch.xavier.backtester.strategy.Strategies;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.*;

@SpringBootTest
@Slf4j
//@TestPropertySource(properties = "mongodb.host=172.18.42.2")
class MongoResultsRepositoryTest {

    @Autowired
    private MongoResultsRepository resultsRepository;


    @Test
    public void listAllCollectionsName() {
        resultsRepository.listAllCollectionsName()
                .doOnNext(name -> log.info("name:{}", name))
                .blockLast();
    }

    @Test
    public void geResultsOfNBestValuesForFieldForStrategy() {
        resultsRepository.geResultsOfNBestValuesForFieldForStrategy(
                3, "avgProfitTrades", Strategies.GlobalExtremaStrategy, QuoteType.DAILY)
                .doOnNext(result -> log.info("Result:{}", result))
                .blockLast();
    }

    @Test
    public void getExistingStrategiesParameters() {
        // GIVEN
        String TEST_COLLECTION_NAME = "GlobalExtremaStrategy - DAILY";

        // WHEN
        Flux resultsParams = resultsRepository.getParametersOfAlreadyAnalyzedStrategies("FB", Strategies.GlobalExtremaStrategy, TEST_COLLECTION_NAME);

        // THEN
        resultsParams.doOnNext(result -> log.info("params:{}", result))
                .blockLast();
    }

    @Test
    public void geResultsOfNBestValuesForFieldForStrategy2() {
        Strategies currentStrat = Strategies.ADXStrategy;
        Map<QuoteType, List<StrategyResult>> bestResults = new HashMap<>();

        log.info("Best results:{}", bestResults);

//        resultsRepository.getNBestValuesOfFieldInCollection(
//                3, "avgProfitTrades", Strategies.ADXStrategy, QuoteType.DAILY)
//                .doOnNext(result -> log.info("Result:{}", result))
//                .blockLast();
    }

    @Test
    public void findBestStrategyOnAverageProfit() {
        resultsRepository.listAllCollectionsName()
                .flatMap(collection -> resultsRepository.getNBestValuesOfFieldInCollection(5, "avgProfit", collection))
                .sort(Comparator.reverseOrder())
                .doOnNext(value -> log.info("Value:{}", value))
                .blockLast();
    }
}