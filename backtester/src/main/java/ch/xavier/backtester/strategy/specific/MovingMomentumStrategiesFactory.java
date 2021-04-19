package ch.xavier.backtester.strategy.specific;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Moving momentum strategy.
 *
 * @see <a href=
 * "https://school.stockcharts.com/doku.php?id=trading_strategies:moving_momentum">
 * https://school.stockcharts.com/doku.php?id=trading_strategies:moving_momentum</a>
 */
@Slf4j
public final class MovingMomentumStrategiesFactory {

    public Flux<Strategy> generateVariationsOfThirdStepWithTypeMA(BarSeries series) {
        //6'000 variations
        Flux<Boolean> useEmaFlux = Flux.just(false, true);
        Flux<Integer> lowMacD = Flux.range(5, 10);
        Flux<Integer> highMacD = Flux.range(25, 15);
        Flux<Integer> emaMacDDays = Flux.range(10, 20);

        //TODO: Don't do that. Get the best movingAverage strat for the first phase from the other optimizations

        log.info("Now creating 6000 variations of the Moving Momentum Strategy with a changing third step and a choice of EMA9-26/SMA20-150");

        return useEmaFlux.flatMap(useEma ->
                lowMacD.flatMap(lowMac ->
                        highMacD.flatMap(highMac ->
                                emaMacDDays.map(emaMac ->
                                        buildStrategyWithChangingThirdStepAndChoiceOfMA(
                                                series, useEma, lowMac, highMac, emaMac)))));
    }

    private Strategy buildStrategyWithChangingThirdStepAndChoiceOfMA(BarSeries series,
                                                                           boolean useEMA, int lowMacD, int highMacD, int emaMacDDays) {
        if (useEMA) {
            return buildStrategy(series, 9, 26, true, 14,
                    20, lowMacD, highMacD, emaMacDDays);
        } else {
            return buildStrategy(series, 20, 150, false, 14,
                    20, lowMacD, highMacD, emaMacDDays);
        }
    }

    public Flux<Strategy> generateAllVariationsOfStrategyWithDefaultEMA(BarSeries series) {
        //1'200'000 variations
        Flux<Integer> stochasticIndicatorDays = Flux.range(10, 20);
        Flux<Integer> stochasticIndicatorThreshold = Flux.range(10, 20);

        Flux<Integer> lowMacD = Flux.range(5, 10);
        Flux<Integer> highMacD = Flux.range(25, 15);
        Flux<Integer> emaMacDDays = Flux.range(10, 20);

        return stochasticIndicatorDays.flatMap(stoDays ->
                stochasticIndicatorThreshold.flatMap(stoThreshold ->
                        lowMacD.flatMap(lowMac ->
                                highMacD.flatMap(highMac ->
                                        emaMacDDays.map(emaMac ->
                                                buildStrategy(
                                                        series, 9, 16,
                                                        true, stoDays, stoThreshold,
                                                        lowMac, highMac, emaMac))))));
    }

    public Flux<Strategy> generateVariationsOfThirdStep(BarSeries series) {
        //3'000 variations
        Flux<Integer> lowMacD = Flux.range(5, 10);
        Flux<Integer> highMacD = Flux.range(25, 15);
        Flux<Integer> emaMacDDays = Flux.range(10, 20);

        return lowMacD.flatMap(lowMac ->
                highMacD.flatMap(highMac ->
                        emaMacDDays.map(emaMac ->
                                buildStrategy(
                                        series, 9, 16,
                                        true, 14, 20,
                                        lowMac, highMac, emaMac))));
    }


    /**
     * @param series the bar series
     * @return the moving momentum strategy
     */
    private Strategy buildStrategy(BarSeries series,
                                         int shortSMADays, int longSMADays, boolean useEMA,
                                         int stochasticIndicatorDays, int stochasticIndicatorThreshold,
                                         int lowMacD, int highMacD, int emaMacDDays) {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        CachedIndicator shortMovingAverageIndicator;
        CachedIndicator longMovingAverageIndicator;

        if (useEMA) {
            shortMovingAverageIndicator = new EMAIndicator(closePrice, shortSMADays);
            longMovingAverageIndicator = new EMAIndicator(closePrice, longSMADays);
        } else {
            shortMovingAverageIndicator = new SMAIndicator(closePrice, shortSMADays); //default 20, 5-50
            longMovingAverageIndicator = new SMAIndicator(closePrice, longSMADays); //default 150, 51-200
        }

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, stochasticIndicatorDays); //default 20, 10-30

        MACDIndicator macd = new MACDIndicator(closePrice, lowMacD, highMacD); //low default 9, 5-15, high default 26, 25-40
        EMAIndicator emaMacd = new EMAIndicator(macd, emaMacDDays); //Default 18, 10-30

        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortMovingAverageIndicator, longMovingAverageIndicator) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, stochasticIndicatorThreshold)) // Signal 1 Default 20, 10-30
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortMovingAverageIndicator, longMovingAverageIndicator) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, stochasticIndicatorThreshold)) // Signal 1 Default 20, 10-30
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal

        Map<String, String> strategyParameters = createStrategyParameters(shortSMADays, longSMADays, useEMA,
                stochasticIndicatorDays, stochasticIndicatorThreshold, lowMacD, highMacD, emaMacDDays);

        return new BaseStrategy("MovingMomentumStrategy", strategyParameters, entryRule, exitRule, 0);
    }

    public Strategy buildDefaultStrategy(BarSeries series) {
        return buildStrategy(series, 9, 26, true,
                14, 20, 9, 26, 18);
    }


    private Map<String, String> createStrategyParameters(int shortSMADays, int longSMADays, boolean useEMA,
                                                                int stochasticIndicatorDays, int stochasticIndicatorThreshold,
                                                                int lowMacD, int highMacD, int emaMacDDays) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("shortSMADays", String.valueOf(shortSMADays));
        parameters.put("longSMADays", String.valueOf(longSMADays));
        parameters.put("useEma", String.valueOf(useEMA));
        parameters.put("stochasticIndicatorDays", String.valueOf(stochasticIndicatorDays));
        parameters.put("stochasticIndicatorThreshold", String.valueOf(stochasticIndicatorThreshold));
        parameters.put("lowMacD", String.valueOf(lowMacD));
        parameters.put("highMacD", String.valueOf(highMacD));
        parameters.put("emaMacDDays", String.valueOf(emaMacDDays));

        return parameters;
    }

    private String generateStrategyName(int shortSMADays, int longSMADays,
                                               boolean useEMA, int stochasticIndicatorDays, int stochasticIndicatorThreshold,
                                               int lowMacD, int highMacD, int emaMacDDays) {

        return shortSMADays + "," + longSMADays + "," + useEMA + "," + stochasticIndicatorDays + "," +
                stochasticIndicatorThreshold + "," + lowMacD + "," + highMacD + "," + emaMacDDays;

//        return "MovingMomentumStrategy under optimization with " +
//                shortSMADays + " days for short term SMA, " +
//                longSMADays + " days for long term SMA, " +
//                "using EMAIndicator instead of SMA:" + useEMA + ", " +
//                stochasticIndicatorDays + " days for the stochastic indicator, " +
//                stochasticIndicatorThreshold + " as a threshold for the stochastic indicator, " +
//                lowMacD + " as a short day count for the MacDIndicator, " +
//                highMacD + " as a long day count for the MacDIndicator, " +
//                emaMacDDays + " as the days for the EMA indicator of the MacDIndicator";
    }
}
