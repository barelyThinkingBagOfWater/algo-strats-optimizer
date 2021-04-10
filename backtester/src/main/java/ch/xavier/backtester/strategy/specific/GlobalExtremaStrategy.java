package ch.xavier.backtester.strategy.specific;

import ch.xavier.backtester.strategy.analyzer.Analyzable;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.util.Map;

/**
 * Strategies which compares current price to global extrema over a week.
 * To be tested and read more about it to make it better
 */
@Slf4j
@ToString
public class GlobalExtremaStrategy implements AnalyzableStrategy {

    private static final int NB_BARS_FOR_ONE_MIN_QUOTES = 60 * 24 * 7;
    private static final int NB_BARS_FOR_FIVE_MIN_QUOTES = 12 * 24 * 7;
    private static final int NB_BARS_FOR_FIFTEEN_MIN_QUOTES = 4 * 24 * 7;
    private static final int NB_BARS_FOR_THIRTY_MIN_QUOTES = 2 * 24 * 7;
    private static final int NB_BARS_FOR_ONE_HOUR_QUOTES = 24 * 7;
    private static final int NB_BARS_FOR_DAILY_QUOTES = 7;

    //How do I use this? Do I refactor everything? Now testing the strats to see the impacts of this field
    enum NB_BARS_FOR_QUOTES {
        ONE_MIN(NB_BARS_FOR_ONE_MIN_QUOTES),
        FIVE_MIN(NB_BARS_FOR_FIVE_MIN_QUOTES),
        FIFTEEN_MIN(NB_BARS_FOR_FIFTEEN_MIN_QUOTES),
        THIRTY_MIN(NB_BARS_FOR_THIRTY_MIN_QUOTES),
        ONE_HOUR(NB_BARS_FOR_ONE_HOUR_QUOTES),
        ONE_DAY(NB_BARS_FOR_DAILY_QUOTES);

        private final int value;

        NB_BARS_FOR_QUOTES(int value) {
            this.value = value;
        }
    }

    @Analyzable(minValue = 1, maxValue = 10)
    private final int rangeInPercent;

    @Analyzable(additionalValues = {
            NB_BARS_FOR_ONE_MIN_QUOTES,
            NB_BARS_FOR_FIVE_MIN_QUOTES,
            NB_BARS_FOR_FIFTEEN_MIN_QUOTES,
            NB_BARS_FOR_THIRTY_MIN_QUOTES,
            NB_BARS_FOR_ONE_HOUR_QUOTES,
            NB_BARS_FOR_DAILY_QUOTES})
    private final int numberOfBars;


    public GlobalExtremaStrategy(int rangeInPercent, int numberOfBars) {
        this.rangeInPercent = rangeInPercent;
        this.numberOfBars = numberOfBars;
    }


    @Override
    public Strategy buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrices = new ClosePriceIndicator(series);

        // Getting the high price over the past week
        HighPriceIndicator highPrices = new HighPriceIndicator(series);
        HighestValueIndicator weekHighPrice = new HighestValueIndicator(highPrices, numberOfBars);
        // Getting the low price over the past week
        LowPriceIndicator lowPrices = new LowPriceIndicator(series);
        LowestValueIndicator weekLowPrice = new LowestValueIndicator(lowPrices, numberOfBars);

        // Going long if the close price goes below the low price
        MultiplierIndicator downWeek = new MultiplierIndicator(weekLowPrice, 1 + (double) rangeInPercent / 1000);
        Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

        // Going short if the close price goes above the high price
        MultiplierIndicator upWeek = new MultiplierIndicator(weekHighPrice, 1 - (double) rangeInPercent / 1000);
        Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);


        return new BaseStrategy(this.getClass().getSimpleName(), getStrategyParameters(), buyingRule, sellingRule, 0);
    }

    @Override
    public Map<String, String> getStrategyParameters() {
        return Map.of(
                "rangeInPercent", String.valueOf(rangeInPercent),
                "numberOfBars", String.valueOf(numberOfBars)
        );
    }
}
