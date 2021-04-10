package ch.xavier.backtester.result.specific;

import ch.xavier.backtester.result.StrategyResult;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@SuperBuilder
@NoArgsConstructor
public class GlobalExtremaStrategyResult extends StrategyResult {

    //Global Extrema Strategy Details
    @Indexed
    private  int rangeInPercent;
    @Indexed
    private int numberOfBars;

    @Override
    public String toString() {
        return "GlobalExtremaStrategyResult{" +
                "rangeInPercent=" + rangeInPercent +
                ", numberOfBars=" + numberOfBars +
                ", " + super.toString() +
                '}';
    }

    @Override
    public Object[] getParams() {
        return new Object[]{ rangeInPercent, numberOfBars };
    }
}