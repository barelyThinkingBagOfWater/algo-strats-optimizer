package ch.xavier.tradingbot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.Map;

public interface RunnableStrategy {
    Strategy buildStrategy(BarSeries series);

    String getNameForActor();

    Map<String, String> getStrategyParameters();

    int getNumberOfQuotesUsedByStrategy();
}
