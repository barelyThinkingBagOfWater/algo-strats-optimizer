package ch.xavier.backtester.result.specific;

import ch.xavier.backtester.result.StrategyResult;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@SuperBuilder
@NoArgsConstructor
public class CciCorrectionsStrategyResult extends StrategyResult {

    //Cci Corrections Strategy Details
    @Indexed
    private int shortCci;
    @Indexed
    private int longCci;
    @Indexed
    private int unstablePeriod;

    @Override
    public String toString() {
        return "CciCorrectionsStrategyResult{" +
                "shortCci=" + shortCci +
                ", longCci=" + longCci +
                ", unstablePeriod=" + unstablePeriod +
                ", " + super.toString() +
                '}';
    }

    @Override
    public Object[] getParams() {
        return new Object[]{ shortCci, longCci, unstablePeriod };
    }
}