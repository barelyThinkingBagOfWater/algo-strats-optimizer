/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2020 Ta4j Organization & respective
 * authors (see AUTHORS)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.xavier.backtester.strategy.specific;

import ch.xavier.backtester.strategy.analyzer.Analyzable;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.Map;

/**
 * ADX indicator based strategy
 *
 * @see <a href=
 * "http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx">
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:average_directional_index_adx</a>
 */
@Slf4j
@ToString
public class ADXStrategy implements AnalyzableStrategy {

    @Analyzable(minValue = 7, maxValue = 21)
    private final int adxDays; //default 14
    @Analyzable(minValue = 10, maxValue = 30)
    private final int overAdxIndicatorDays; //default 20
    @Analyzable(additionalValues = {10, 15, 20, 30, 50, 100, 150, 200})
    private final int smaDays;


    public ADXStrategy(final int adxDays, final int overAdxIndicatorDays, final int smaDays) {
        this.adxDays = adxDays;
        this.overAdxIndicatorDays = overAdxIndicatorDays;
        this.smaDays = smaDays;
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        final ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        final SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, smaDays);

        final ADXIndicator adxIndicator = new ADXIndicator(series, adxDays);
        final OverIndicatorRule adxOver20Rule = new OverIndicatorRule(adxIndicator, overAdxIndicatorDays);

        final PlusDIIndicator plusDIIndicator = new PlusDIIndicator(series, adxDays);
        final MinusDIIndicator minusDIIndicator = new MinusDIIndicator(series, adxDays);

        final Rule plusDICrossedUpMinusDI = new CrossedUpIndicatorRule(plusDIIndicator, minusDIIndicator);
        final Rule plusDICrossedDownMinusDI = new CrossedDownIndicatorRule(plusDIIndicator, minusDIIndicator);
        final OverIndicatorRule closePriceOverSma = new OverIndicatorRule(closePriceIndicator, smaIndicator);
        final Rule entryRule = adxOver20Rule.and(plusDICrossedUpMinusDI).and(closePriceOverSma);

        final UnderIndicatorRule closePriceUnderSma = new UnderIndicatorRule(closePriceIndicator, smaIndicator);
        final Rule exitRule = adxOver20Rule.and(plusDICrossedDownMinusDI).and(closePriceUnderSma);

        //unstable period (last param below) also a parameter?
        return new BaseStrategy(this.getClass().getSimpleName(), getStrategyParameters(), entryRule, exitRule, adxDays);
    }

    @Override
    public Map<String, String> getStrategyParameters() {
        return Map.of(
                "adxDays", String.valueOf(adxDays),
                "overAdxIndicatorDays", String.valueOf(overAdxIndicatorDays),
                "smaDays", String.valueOf(smaDays)
        );
    }
}
