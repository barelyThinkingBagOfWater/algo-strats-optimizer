package ch.xavier.backtester.result;

import ch.xavier.backtester.quote.typed.QuoteType;
import ch.xavier.backtester.strategy.Strategies;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;

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
    public void findBestStrategyForCriterion() {
        // GIVEN
        Strategies strategy = Strategies.GlobalExtremaStrategy;
        String symbol = "FB";
        //for now net profit as no commission on Apalca, check after the realism of this choice and which criterion
        //to really use
        String criterion = "netProfit";

        // WHEN
        resultsRepository.getBestParametersForStrategy(strategy, criterion)
                .sort(Comparator.comparingDouble(StrategyResult::getNetProfit))
                .doOnNext(result -> log.info("Result:{}", result))
                .blockLast();
    }

    @Test
    public void findBestStrategyForCriterionForOneMinQuote() {
        // GIVEN
        Strategies strategy = Strategies.GlobalExtremaStrategy;
        String symbol = "FB";
        //for now net profit as no commission on Apalca, check after the realism of this choice and which criterion
        //to really use
        String criterion = "avgProfit";

        // WHEN
        resultsRepository.geResultsOfNBestValuesForFieldForStrategy(criterion, strategy, QuoteType.ONE_MIN)
                .sort(Comparator.comparingDouble(StrategyResult::getAvgProfit))
                .doOnNext(result -> log.info("Result:{}", result))
                .blockLast();
    }
}