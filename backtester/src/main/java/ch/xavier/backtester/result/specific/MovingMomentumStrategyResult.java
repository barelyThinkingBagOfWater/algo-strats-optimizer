package ch.xavier.backtester.result.specific;

import ch.xavier.backtester.result.StrategyResult;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@SuperBuilder
@NoArgsConstructor
public class MovingMomentumStrategyResult extends StrategyResult {

    //Moving Momentum Strategy Details
    @Indexed
    private int shortSMADays;
    @Indexed
    private int longSMADays;
    @Indexed
    private boolean useEMA;
    @Indexed
    private int stochasticIndicatorDays;
    @Indexed
    private int stochasticIndicatorThreshold;
    @Indexed
    private int lowMacD;
    @Indexed
    private int highMacD;
    @Indexed
    private int emaMacDDays;


    @Override
    public String toString() {
        return "MovingMomentumStrategyResult{" +
                "shortSMADays=" + shortSMADays +
                ", longSMADays=" + longSMADays +
                ", useEMA=" + useEMA +
                ", stochasticIndicatorDays=" + stochasticIndicatorDays +
                ", stochasticIndicatorThreshold=" + stochasticIndicatorThreshold +
                ", lowMacD=" + lowMacD +
                ", highMacD=" + highMacD +
                ", emaMacDDays=" + emaMacDDays +
                ", " + super.toString() +
                '}';
    }

    @Override
    public Object[] getParams() {
        return new Object[] { shortSMADays, longSMADays, useEMA, stochasticIndicatorDays, stochasticIndicatorThreshold,
                lowMacD, highMacD, emaMacDDays };
    }
}
