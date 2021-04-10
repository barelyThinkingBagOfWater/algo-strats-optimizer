package ch.xavier.backtester.strategy.analyzer;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.Map;

public interface AnalyzableStrategy {
    Strategy buildStrategy(BarSeries series);

    Map<String, String> getStrategyParameters();
}
