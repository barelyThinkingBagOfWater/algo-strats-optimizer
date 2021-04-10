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
    private double avgProfitTrades;
    @Indexed
    private double avgProfit;
    @Indexed
    private double totalProfit;
    @Indexed
    private double buyAndHold;
    @Indexed
    private double numTrades;
    @Indexed
    private double vsByAndHold;
    @Indexed
    private double rewardRiskRatio;
    @Indexed
    private double maxDrawDown;

    //Strategy details
    @Indexed
    private String strategyName;

    @Override
    public String toString() {
        return "StrategyResult{" +
                "symbol='" + symbol + '\'' +
                ", avgProfitTrades=" + avgProfitTrades +
                ", avgProfit=" + avgProfit +
                ", totalProfit=" + totalProfit +
                ", buyAndHold=" + buyAndHold +
                ", numTrades=" + numTrades +
                ", vsByAndHold=" + vsByAndHold +
                ", rewardRiskRatio=" + rewardRiskRatio +
                ", maxDrawDown=" + maxDrawDown +
                ", strategyName='" + strategyName + '\'' +
                '}';
    }

    public abstract Object[] getParams();
}
