package ch.xavier.backtester.strategy.specific;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.util.HashMap;
import java.util.Map;

/**
 * From https://tradingsim.com/blog/5-minute-bar/, strategy #2
 * With knowledge about the indicators from the page in their JavaDoc
 *
 * TO FINISH
 */
public class MacdAndMfiStrategiesFactory {

    public Strategy buildDefaultStrategy(BarSeries series) {

        //Start with moving averages first step as usual? Add when optimized?

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaIndicator = new EMAIndicator(closePrice, 9);

        ChaikinMoneyFlowIndicator moneyFlowIndicator = new ChaikinMoneyFlowIndicator(series, 14);

        // Entry rule
        Rule entryRule = new OverIndicatorRule(macd, emaIndicator)
                .and(new CrossedUpIndicatorRule(moneyFlowIndicator, 0)); //May be wrong...

        // Exit rule
        Rule exitRule = new UnderIndicatorRule(macd, emaIndicator)
                .and(new CrossedDownIndicatorRule(moneyFlowIndicator, 0));

        return new BaseStrategy("MovingMomentumStrategy", entryRule, exitRule);
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
}
