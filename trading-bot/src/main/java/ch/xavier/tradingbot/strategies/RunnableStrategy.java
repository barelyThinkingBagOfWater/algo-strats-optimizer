package ch.xavier.tradingbot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface RunnableStrategy {
    Strategy buildStrategy(BarSeries series);
}
