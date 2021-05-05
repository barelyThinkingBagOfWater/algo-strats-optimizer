package ch.xavier.backtester.strategy;

import ch.xavier.backtester.result.StrategyResult;
import ch.xavier.backtester.result.specific.AdxStrategyResult;
import ch.xavier.backtester.result.specific.CciCorrectionsStrategyResult;
import ch.xavier.backtester.result.specific.GlobalExtremaStrategyResult;
import ch.xavier.backtester.result.specific.MovingAveragesStrategyResult;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import ch.xavier.backtester.strategy.specific.ADXStrategy;
import ch.xavier.backtester.strategy.specific.CCICorrectionStrategy;
import ch.xavier.backtester.strategy.specific.GlobalExtremaStrategy;
import ch.xavier.backtester.strategy.specific.MovingAveragesStrategy;

import java.util.Set;


public enum Strategies {
    ADXStrategy(ADXStrategy.class, 2520, AdxStrategyResult.class, Set.of("adxDays", "smaDays", "overAdxIndicatorDays")),
    CCICorrectionStrategy(CCICorrectionStrategy.class, 2400, CciCorrectionsStrategyResult.class, Set.of("shortCci", "longCci", "unstablePeriod")),
    GlobalExtremaStrategy(GlobalExtremaStrategy.class, 60, GlobalExtremaStrategyResult.class, Set.of("rangeInPercent", "numberOfBars")),
    MovingAveragesStrategy(MovingAveragesStrategy.class, 589824, MovingAveragesStrategyResult.class, Set.of("buyShortMA", "buyLongMA", "sellShortMA", "sellLongMA", "movingAverageType"));


    private final Class<? extends AnalyzableStrategy> className;
    private final int variationsCountPerSymbol;
    private final Class<? extends StrategyResult> resultClassName;
    private final Set<String> specificParameters; //aka strat.getSpecificParameters.keyset()


    Strategies(Class<? extends AnalyzableStrategy> className, int variationsCountPerSymbol,
               Class<? extends StrategyResult> resultClassName, Set<String> specificParameters) {
        this.className = className;
        this.variationsCountPerSymbol = variationsCountPerSymbol;
        this.resultClassName = resultClassName;
        this.specificParameters = specificParameters;
    }


    public Class<? extends AnalyzableStrategy> className() {
        return this.className;
    }

    public int variationsCountPerSymbol() {
        return this.variationsCountPerSymbol;
    }

    public Class<? extends StrategyResult> resultClassName() {
        return this.resultClassName;
    }

    public Set<String> specificParameters() {
        return this.specificParameters;
    }
}
