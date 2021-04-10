package ch.xavier.backtester.strategy;

import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
class StrategiesFactoryTest {

    private final StrategiesFactory strategiesFactory = StrategiesFactory.INSTANCE;

    @Test
    public void display_generated_strategies() {
        // GIVEN
        Strategies testedStrategy = Strategies.ADXStrategy;

        // WHEN
        Flux<AnalyzableStrategy> strategies = strategiesFactory.generateAllVariations(testedStrategy, Flux.empty());

        // THEN
        strategies
//                .doOnNext(strategy -> log.info("strat:{}", strategy))
                .count()
                .doOnNext(count -> log.info("count:{}", count))
        .block();
    }

    @Test
    public void factory_generates_correct_number_of_variations_for_each_strategy() {
        // GIVEN
        Flux<Strategies> analyzableStrategies = Flux.fromArray(Strategies.values());

        // WHEN
        analyzableStrategies
                .doOnNext(strategy -> {
                    strategiesFactory.generateAllVariations(strategy, Flux.empty())
                            .count()

                            // THEN
                            .doOnNext(count -> Assertions.assertEquals(strategy.variationsCountPerSymbol(), count))
                    .subscribe();
                }).blockLast();
    }
}