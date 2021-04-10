package ch.xavier.backtester.result.specific;

import ch.xavier.backtester.result.StrategyResult;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@SuperBuilder
@NoArgsConstructor
public class AdxStrategyResult extends StrategyResult {

    //Adx Strategy Details
    @Indexed
    private int adxDays;
    @Indexed
    private int overAdxIndicatorDays;
    @Indexed
    private int smaDays;

    @Override
    public String toString() {
        return "AdxStrategyResult{" +
                "adxDays=" + adxDays +
                ", overAdxIndicatorDays=" + overAdxIndicatorDays +
                ", smaDays=" + smaDays +
                ", " +  super.toString() +
                '}';
    }

    @Override
    public Object[] getParams() {
        return new Object[]{ adxDays, overAdxIndicatorDays, smaDays };
    }
}