package ch.xavier.backtester.strategy.specific;


import ch.xavier.backtester.strategy.analyzer.Analyzable;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;


/**
 * Simple strategies using a crossing of moving averages to buy/sell orders
 */
@Slf4j
@ToString
public final class MovingAveragesStrategy implements AnalyzableStrategy {

    public enum MovingAverageType { //9
        EMA, DoubleEMA, TripleEMA, HMA, KAMA, LWMA, MMA, ZLEMA, SMA;
    }

    @Analyzable(minValue = 3, maxValue = 15, additionalValues = {20, 25, 30})
    private final int buyShortMA;
    @Analyzable(minValue = 9, maxValue = 20, additionalValues = {50, 100, 150, 200})
    private final int buyLongMA;
    @Analyzable(minValue = 3, maxValue = 15, additionalValues = {20, 25, 30})
    private final int sellShortMA;
    @Analyzable(minValue = 9, maxValue = 20, additionalValues = {50, 100, 150, 200})
    private final int sellLongMA;
    @Analyzable
    private final MovingAverageType maType;


    public MovingAveragesStrategy(int buyShortMA, int buyLongMA, int sellShortMA, int sellLongMA,
                                  MovingAverageType maType) {
        this.buyShortMA = buyShortMA;
        this.buyLongMA = buyLongMA;
        this.sellShortMA = sellShortMA;
        this.sellLongMA = sellLongMA;
        this.maType = maType;
    }


    @Override
    public Strategy buildStrategy(BarSeries series) {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        CachedIndicator buyShortMAIndicator;
        CachedIndicator buyLongMAIndicator;
        CachedIndicator sellShortMAIndicator;
        CachedIndicator sellLongMAIndicator;

        switch (maType) {
            case EMA -> {
                buyShortMAIndicator = new EMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new EMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new EMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new EMAIndicator(closePrice, sellLongMA);
            }
            case HMA -> {
                buyShortMAIndicator = new HMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new HMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new HMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new HMAIndicator(closePrice, sellLongMA);
            }
            case MMA -> {
                buyShortMAIndicator = new MMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new MMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new MMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new MMAIndicator(closePrice, sellLongMA);
            }
            case DoubleEMA -> {
                buyShortMAIndicator = new DoubleEMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new DoubleEMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new DoubleEMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new DoubleEMAIndicator(closePrice, sellLongMA);
            }
            case KAMA -> {
                buyShortMAIndicator = new KAMAIndicator(closePrice, buyShortMA, 2, 30);
                buyLongMAIndicator = new KAMAIndicator(closePrice, buyLongMA, 2, 30);
                sellShortMAIndicator = new KAMAIndicator(closePrice, sellShortMA, 2, 30);
                sellLongMAIndicator = new KAMAIndicator(closePrice, sellLongMA, 2, 30);
            }
            case LWMA -> {
                buyShortMAIndicator = new LWMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new LWMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new LWMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new LWMAIndicator(closePrice, sellLongMA);
            }
            case ZLEMA -> {
                buyShortMAIndicator = new ZLEMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new ZLEMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new ZLEMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new ZLEMAIndicator(closePrice, sellLongMA);
            }
            case TripleEMA -> {
                buyShortMAIndicator = new TripleEMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new TripleEMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new TripleEMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new TripleEMAIndicator(closePrice, sellLongMA);
            }
            case SMA -> {
                buyShortMAIndicator = new SMAIndicator(closePrice, buyShortMA);
                buyLongMAIndicator = new SMAIndicator(closePrice, buyLongMA);
                sellShortMAIndicator = new SMAIndicator(closePrice, sellShortMA);
                sellLongMAIndicator = new SMAIndicator(closePrice, sellLongMA);
            }
            default -> throw new IllegalArgumentException("Moving average type not yet implemented: " + maType);
        }

        Rule entryRule = new OverIndicatorRule(buyShortMAIndicator, buyLongMAIndicator);

        Rule exitRule = new UnderIndicatorRule(sellShortMAIndicator, sellLongMAIndicator);

        return new BaseStrategy(this.getClass().getSimpleName(), getStrategyParameters(), entryRule, exitRule, 0);
    }

    @Override
    public Map<String, String> getStrategyParameters() {
        return Map.of(
                "buyShortMA", String.valueOf(buyShortMA),
                "buyLongMA", String.valueOf(buyLongMA),
                "sellShortMA", String.valueOf(sellShortMA),
                "sellLongMA", String.valueOf(sellLongMA),
                "movingAverageType", maType.name()
        );
    }
}
