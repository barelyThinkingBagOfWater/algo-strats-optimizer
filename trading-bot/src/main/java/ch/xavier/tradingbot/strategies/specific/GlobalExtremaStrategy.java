package ch.xavier.tradingbot.strategies.specific;

import ch.xavier.tradingbot.strategies.RunnableStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public record GlobalExtremaStrategy(int rangeInPercent,
                                    int numberOfBars) implements RunnableStrategy {

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
        TransformIndicator downWeek = TransformIndicator.multiply(weekLowPrice, 1 + (double) rangeInPercent / 1000);
        Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

        // Going short if the close price goes above the high price
        TransformIndicator upWeek = TransformIndicator.multiply(weekHighPrice, 1 - (double) rangeInPercent / 1000);
        Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);


        return new BaseStrategy(this.getClass().getSimpleName(), buyingRule, sellingRule, 0);
    }
}
