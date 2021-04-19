package ch.xavier.backtester.strategy.analyzer;

import ch.xavier.backtester.quote.MongoQuotesRepository;
import ch.xavier.backtester.quote.Quote;
import ch.xavier.backtester.quote.SymbolsRegistry;
import ch.xavier.backtester.quote.typed.QuoteType;
import ch.xavier.backtester.result.MongoResultsRepository;
import ch.xavier.backtester.result.ResultsFactory;
import ch.xavier.backtester.result.StrategyResult;
import ch.xavier.backtester.strategy.Strategies;
import ch.xavier.backtester.strategy.StrategiesFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StrategiesAnalyzer {

    private final MongoQuotesRepository quotesRepository;
    private final MongoResultsRepository resultsRepository;
    private final StrategiesFactory strategiesFactory;

    @Autowired
    //TODO: Add constraints in annotations, like param1 > param2.
    public StrategiesAnalyzer(MongoQuotesRepository quotesRepository, MongoResultsRepository resultsRepository) {
        this.quotesRepository = quotesRepository;
        this.resultsRepository = resultsRepository;
        this.strategiesFactory = StrategiesFactory.INSTANCE;

//        analyzeStrategyOnSymbolsWithAllQuotes(Flux.just("FB"), Strategies.ADXStrategy)
//        analyzeStrategyOnSymbolsWithQuotes(SymbolsRegistry.MOST_TRADED_US_SYMBOLS, QuoteType.ONE_MIN, Strategies.CCICorrectionStrategy)
//        analyzeStrategyOnSymbolsWithQuotes(SymbolsRegistry.MOST_TRADED_US_SYMBOLS, QuoteType.HOURLY, Strategies.ADXStrategy)
//                .blockLast();
    }

    public Flux<StrategyResult> analyzeStrategyOnSymbolsWithAllQuotes(final Flux<String> symbols, final Strategies strategy) {
        return analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.DAILY, strategy)
                .thenMany(analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.HOURLY, strategy))
                .thenMany(analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.THIRTY_MIN, strategy))
                .thenMany(analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.FIFTEEN_MIN, strategy))
                .thenMany(analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.FIVE_MIN, strategy))
                .thenMany(analyzeStrategyOnSymbolsWithQuotes(symbols, QuoteType.ONE_MIN, strategy));
    }

    public Flux<StrategyResult> analyzeStrategyOnSymbolsWithQuotes(final Flux<String> symbols, final QuoteType quoteType,
                                                                   final Strategies strategy) {

        final String collectionName = strategy.name() + " - " + quoteType.name();

        return resultsRepository.createCollectionAndIndexIfNeeded(collectionName, strategy.specificParameters())
                .then(displayProgressDetails(symbols.count(), strategy, resultsRepository, collectionName))
                .thenMany(symbols
                        .filter(symbol -> !isSymbolAlreadyAnalyzed(strategy.variationsCountPerSymbol(), collectionName, symbol))
                        .flatMap(symbol -> analyzeStrategyOnSymbolWithQuotes(symbol, quoteType, strategy, collectionName)))
                .doOnComplete(() -> {
                    log.info("Analyze done for {}, have a great day!", collectionName);
                    Schedulers.elastic().dispose();
                });
    }


    private Flux<StrategyResult> analyzeStrategyOnSymbolWithQuotes(final String symbol, final QuoteType quoteType,
                                                                   final Strategies strategy, String collectionName) {

        final BaseBarSeries series = new BaseBarSeriesBuilder().withName(collectionName + " - " + symbol).build();
        final Flux<Object[]> combinationsOfParametersToExclude = resultsRepository.getParametersOfAlreadyAnalyzedStrategies(symbol, strategy, collectionName);
        final Flux<TradingRecord> results = runStrategyOnSeries(strategy, series, combinationsOfParametersToExclude);

        return fillSeriesWithQuotesOfSymbol(series, symbol, quoteType)
                .thenMany(saveResults(strategy, series, results, collectionName, symbol));
    }


    private Flux<Bar> fillSeriesWithQuotesOfSymbol(BaseBarSeries series, final String symbol, final QuoteType quoteType) {
        return quotesRepository
                .findAllBySymbol(symbol, quoteType)
                .map(Quote::toBar)
                .doOnNext(series::addBar);
    }

    public Flux<TradingRecord> runStrategyOnSeries(final Strategies analyzableStrategy,
                                                   final BaseBarSeries series,
                                                   Flux<Object[]> combinationsOfParametersToExclude) {

        final BarSeriesManager manager = new BarSeriesManager(series);

        return strategiesFactory
                .generateAllVariations(analyzableStrategy, combinationsOfParametersToExclude)
                .map(analyzableStrat -> analyzableStrat.buildStrategy(series))
                //TODO: Doesn't seem to run multiple analysis on all threads, it runs one on a thread, then another on another thread, ... profile this
                .publishOn(Schedulers.parallel())
                .map(manager::run);
    }

    private boolean isSymbolAlreadyAnalyzed(int variationsCountPerSymbol, String collectionName, String symbol) {
        return variationsCountPerSymbol == resultsRepository.countSpecificResultsForSymbolInCollection(symbol, collectionName).block();
    }

    private Flux<StrategyResult> saveResults(Strategies strategy, BaseBarSeries series, Flux<TradingRecord> results,
                                             String collectionName, String symbol) {
        return ResultsFactory.createStorableResultsForStrategy(strategy, series, results, symbol)
                .flatMap(result -> resultsRepository.save(result, collectionName));
    }


    private Mono displayProgressDetails(Mono<Long> multiplier, Strategies strategy, MongoResultsRepository resultsRepository, String collectionName) {
        return Mono.fromRunnable(new ProgressDetailsRunnable(strategy.variationsCountPerSymbol() * multiplier.block(), collectionName, resultsRepository))
                .subscribeOn(Schedulers.elastic());
    }

    private static class ProgressDetailsRunnable implements Runnable {

        private final long totalCount;
        private final long initCount;
        private final String collectionName;
        private final MongoResultsRepository resultsRepository;
        private static final DecimalFormat doubleFormat = new DecimalFormat("#.00");
        private static final Logger log = LoggerFactory.getLogger(StrategiesAnalyzer.class);


        public ProgressDetailsRunnable(final long totalCount, final String collectionName, final MongoResultsRepository resultsRepository) {
            this.initCount = resultsRepository.countResultsInCollection(collectionName).block();
            this.totalCount = totalCount - initCount;
            this.collectionName = collectionName;
            this.resultsRepository = resultsRepository;
        }

        public void run() {
            long startTimeMillis = System.currentTimeMillis();

            Schedulers.newBoundedElastic(12, Integer.MAX_VALUE, "TheProgresser")
                    .schedulePeriodically(() -> {
                        Long currentCount = resultsRepository.countResultsInCollection(collectionName).block() - initCount;

                        if (0 != currentCount) {
                            long currentProgress = (100 * currentCount) / totalCount;

                            if (currentProgress != 100) {
                                long elapsedTimeInSecond = (System.currentTimeMillis() - startTimeMillis) / 1000;

                                String estimatedRemainingTimeInSecond = String.valueOf(
                                        (int) (((double) totalCount / currentCount) * elapsedTimeInSecond) - elapsedTimeInSecond);

                                log.info("""
                                                Analyzing {} variations of {} quotes
                                                Progress:{}%, elapsed seconds:{}, remaining strats:{}, remaining seconds:{}, rate:{} strats/sec\s
                                                """,
                                        totalCount, collectionName, currentProgress, elapsedTimeInSecond,
                                        totalCount - currentCount, estimatedRemainingTimeInSecond,
                                        doubleFormat.format((double) currentCount / elapsedTimeInSecond));
                            }
                        }
                    }, 0, 5, TimeUnit.SECONDS);
        }
    }
}
