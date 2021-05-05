package ch.xavier.backtester.strategy.analyzer;

import ch.xavier.backtester.quote.typed.QuoteType;
import ch.xavier.backtester.result.MongoResultsRepository;
import ch.xavier.backtester.result.StrategyResult;
import ch.xavier.backtester.strategy.Strategies;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
class StrategiesAnalyzerTest {

    @Autowired
    private MongoResultsRepository resultsRepository;

    @Autowired
    private StrategiesAnalyzer analyzer;

    private static final String TEST_COLLECTION_NAME = "GlobalExtremaStrategy - FIVE_MIN";
    private static final Strategies TEST_STRATEGY = Strategies.GlobalExtremaStrategy;
    private static final QuoteType TEST_QUOTE_TYPE = QuoteType.DAILY;



    @Test
    public void run_OneStrat_onFB_withOneMinQuote() {
        // GIVEN
//        resultsRepository.dropCollection(TEST_COLLECTION_NAME).block();
        QuoteType quoteType = QuoteType.ONE_MIN;
        Strategies strat = Strategies.MovingAveragesStrategy;

        // WHEN
        analyzer.analyzeStrategyOnSymbolsWithQuotes(Flux.just("BINANCE:BTCUSDT"), quoteType, strat).blockLast();

        // THEN
//        assertEquals(4920, resultsRepository.countResultsInCollection(TEST_COLLECTION_NAME).block()); //60 variations * 82 symbols
//        assertEquals(60, resultsRepository.countSpecificResultsForSymbolInCollection("FB", TEST_COLLECTION_NAME).block());
    }

    @Test
    public void run_OneStrat_onFB_withFiveMinQuote() {
        // GIVEN
        resultsRepository.dropCollection(TEST_COLLECTION_NAME).block();
        QuoteType quoteType = QuoteType.FIVE_MIN;
        Strategies strat = Strategies.GlobalExtremaStrategy;

        // WHEN
        analyzer.analyzeStrategyOnSymbolsWithQuotes(Flux.just("FB"), quoteType, strat).blockLast();

        // THEN
        assertEquals(60, resultsRepository.countSpecificResultsForSymbolInCollection("FB", TEST_COLLECTION_NAME).block());

        StrategyResult topResult = resultsRepository.geResultsOfNBestValuesForFieldForStrategy("avgProfit", strat, quoteType).blockFirst();
        assertEquals(0.6666666666666666, topResult.getWinningPositionsRatio());
        assertEquals(1.0002632978702277, topResult.getAvgProfit());
        assertEquals(.35, topResult.getGrossProfit());
        assertEquals(.35, topResult.getNetProfit());
        assertEquals(15.9453125, topResult.getBuyAndHold());
    }

    @Test
    public void run_OneStrat_onFB_withAllQuotes() {
        // GIVEN
        String testQuote = "FB";
        Strategies testStrategy = Strategies.ADXStrategy;

        // WHEN
        analyzer.analyzeStrategyOnSymbolsWithAllQuotes(Flux.just(testQuote), testStrategy).blockLast();
    }
}