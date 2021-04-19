package ch.xavier.backtester.strategy.specific;

import ch.xavier.backtester.strategy.analyzer.Analyzable;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

/**
 * CCI Correction Strategy
 *
 * @see <a href=
 *      "http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction">
 *      http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction</a>

Seems more for RT, check https://tradingsim.com/blog/commodity-channel-index/

I didn't really read the doc on this strat, I surmise could be improved, especially with second link
 */
@Slf4j
@ToString
public final class CCICorrectionStrategy implements AnalyzableStrategy {

    @Analyzable(minValue = 1, maxValue = 20)
    private final int shortCci;
    @Analyzable(additionalValues = {20, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 300})
    private final int longCci;
    @Analyzable(minValue = 1, maxValue = 10)
    private final int unstablePeriod;

    public CCICorrectionStrategy(int shortCci, int longCci, int unstablePeriod) {
        this.shortCci = shortCci;
        this.longCci = longCci;
        this.unstablePeriod = unstablePeriod;
    }

    @Override
    public Strategy buildStrategy(final BarSeries series) {
        final CCIIndicator shortCciIndicator = new CCIIndicator(series, shortCci);
        final CCIIndicator longCciIndicator = new CCIIndicator(series, longCci);
        final Num plus100 = series.numOf(100);
        final Num minus100 = series.numOf(-100);

        final Rule entryRule = new OverIndicatorRule(longCciIndicator, plus100)
                .and(new UnderIndicatorRule(shortCciIndicator, minus100));

        final Rule exitRule = new UnderIndicatorRule(longCciIndicator, minus100)
                .and(new OverIndicatorRule(shortCciIndicator, plus100));

        return new BaseStrategy(this.getClass().getSimpleName(), getStrategyParameters(), entryRule, exitRule, unstablePeriod);
    }

    @Override
    public Map<String, String> getStrategyParameters() {
        return Map.of(
                "shortCci", String.valueOf(shortCci),
                "longCci", String.valueOf(longCci),
                "unstablePeriod", String.valueOf(unstablePeriod)
        );
    }
}
