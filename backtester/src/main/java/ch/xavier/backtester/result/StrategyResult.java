package ch.xavier.backtester.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class StrategyResult {

    //Criterion results
    @Indexed
    private String symbol;
    @Indexed
    private double winningPositionsRatio;
    @Indexed
    private double avgProfit;
    @Indexed
    private double grossProfit;
    @Indexed
    private Double netProfit;
    @Indexed
    private double buyAndHold;
    @Indexed
    private double numTrades;
    @Indexed
    private double avgProfitsvsByAndHold;
    @Indexed
    private double netProfitsvsByAndHold;
    @Indexed
    private double returnOverMaxDrawdown;
    @Indexed
    private double maxDrawDown;

    //Strategy details
    @Indexed
    private String strategyName;

    @Override
    public String toString() {
        return "StrategyResult{" +
                "symbol='" + symbol + '\'' +
                ", winningPositionsRatio=" + winningPositionsRatio +
                ", avgProfit=" + avgProfit +
                ", grossProfit=" + grossProfit +
                ", netProfit=" + netProfit +
                ", buyAndHold=" + buyAndHold +
                ", numTrades=" + numTrades +
                ", avgProfitsvsByAndHold=" + avgProfitsvsByAndHold +
                ", netProfitsvsByAndHold=" + netProfitsvsByAndHold +
                ", returnOverMaxDrawdown=" + returnOverMaxDrawdown +
                ", maxDrawDown=" + maxDrawDown +
                ", strategyName='" + strategyName + '\'' +
                '}';
    }

    public abstract Object[] getParams();
}
