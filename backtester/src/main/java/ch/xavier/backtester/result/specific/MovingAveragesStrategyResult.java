package ch.xavier.backtester.result.specific;

import ch.xavier.backtester.result.StrategyResult;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@SuperBuilder
@NoArgsConstructor
public class MovingAveragesStrategyResult extends StrategyResult {

    //Moving Average Strategy Details
    @Indexed
    private int buyShortMA;
    @Indexed
    private int buyLongMA;
    @Indexed
    private int sellShortMA;
    @Indexed
    private int sellLongMA;
    @Indexed
    private String movingAverageType;

    @Override
    public String toString() {
        return "MovingAveragesStrategyResult{" +
                "movingAverageType='" + movingAverageType + '\'' +
                ", buyShortMA=" + buyShortMA +
                ", buyLongMA=" + buyLongMA +
                ", sellShortMA=" + sellShortMA +
                ", sellLongMA=" + sellLongMA +
                super.toString() +
                '}';
    }

    @Override
    public Object[] getParams() {
        return new Object[]{ buyShortMA, buyLongMA, sellShortMA, sellLongMA, movingAverageType };
    }
}
