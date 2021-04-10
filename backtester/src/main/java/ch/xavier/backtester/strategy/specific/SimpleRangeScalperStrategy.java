package ch.xavier.backtester.strategy.specific;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.trading.rules.*;

/**
 * http://www.investopedia.com/terms/s/scalping.asp
 * http://forexop.com/strategy/simple-range-scalper/
 *
 *  STRAT FOR RT
 */
public class SimpleRangeScalperStrategy {

    private static final int emaForBollingerBandValue = 14;
    private static final double takeProfitValue = 0.01d;


    public static Strategy buildShortTermStrategy(BarSeries series) {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema = new EMAIndicator(closePrice, emaForBollingerBandValue);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, emaForBollingerBandValue);
        BollingerBandsMiddleIndicator middleBollingerBand = new BollingerBandsMiddleIndicator(ema);
        BollingerBandsLowerIndicator lowerBollingeBand = new BollingerBandsLowerIndicator(middleBollingerBand, standardDeviation);

        //SHORT TERM start, long term below
        DifferenceIndicator d_middle_lower = new DifferenceIndicator(middleBollingerBand, lowerBollingeBand);
        // exit if half way down to middle reached
        MultiplierIndicator threshold = new MultiplierIndicator(d_middle_lower, 0.5d);

        Rule entrySignal = new CrossedDownIndicatorRule(new LowPriceIndicator(series), lowerBollingeBand); //LowestValueIndicator?
        Rule entrySignal2 = new OverIndicatorRule(new HighPriceIndicator(series), lowerBollingeBand);

        Rule exitSignal = new CrossedUpIndicatorRule(closePrice, threshold);
        Rule exitSignal2 = new StopLossRule(closePrice, takeProfitValue); // stop loss long = stop gain short?

        return new BaseStrategy("SimpleRangeScalperShortTermStrategy", entrySignal.and(entrySignal2), exitSignal.or(exitSignal2));
    }

    public static Strategy buildLongTermStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema = new EMAIndicator(closePrice, emaForBollingerBandValue);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, emaForBollingerBandValue);
        BollingerBandsMiddleIndicator middleBollingerBand = new BollingerBandsMiddleIndicator(ema);
        BollingerBandsUpperIndicator upperBollingerBand = new BollingerBandsUpperIndicator(middleBollingerBand, standardDeviation);

        DifferenceIndicator d_upper_middle = new DifferenceIndicator(upperBollingerBand, middleBollingerBand);
        // exit if half way up to middle reached
        MultiplierIndicator threshold = new MultiplierIndicator(d_upper_middle, Double.valueOf(0.5));

        Rule entrySignal = new CrossedUpIndicatorRule(new HighPriceIndicator(series), upperBollingerBand);
        Rule entrySignal2 = new UnderIndicatorRule(new LowPriceIndicator(series), upperBollingerBand);

        Rule exitSignal = new CrossedDownIndicatorRule(closePrice, threshold);
        Rule exitSignal2 = new StopGainRule(closePrice, takeProfitValue);

        return new BaseStrategy("SimpleRangeScalperLongTermStrategy", entrySignal.and(entrySignal2), exitSignal.or(exitSignal2));
    }
}
 
