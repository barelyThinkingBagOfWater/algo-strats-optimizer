package ch.xavier.backtester.result;

import ch.xavier.backtester.result.specific.*;
import ch.xavier.backtester.strategy.Strategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.*;
import org.ta4j.core.analysis.criteria.pnl.GrossProfitCriterion;
import org.ta4j.core.analysis.criteria.pnl.NetProfitCriterion;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class ResultsFactory {

    //Criterion
    private static final NumberOfPositionsCriterion numTrades = new NumberOfPositionsCriterion();
    private static final WinningPositionsRatioCriterion winningPositionsRatio = new WinningPositionsRatioCriterion();

    private static final AverageReturnPerBarCriterion avgProfit = new AverageReturnPerBarCriterion();
    private static final GrossProfitCriterion grossProfit = new GrossProfitCriterion();
    private static final NetProfitCriterion netProfit = new NetProfitCriterion();

    private static final BuyAndHoldReturnCriterion buyAndHold = new BuyAndHoldReturnCriterion();
    private static final VersusBuyAndHoldCriterion avgProfitsvsByAndHold = new VersusBuyAndHoldCriterion(avgProfit);
    private static final VersusBuyAndHoldCriterion netProfitsvsByAndHold = new VersusBuyAndHoldCriterion(netProfit);

    private static final MaximumDrawdownCriterion maxDrawDown = new MaximumDrawdownCriterion();
    private static final ReturnOverMaxDrawdownCriterion returnOverMaxDrawdown = new ReturnOverMaxDrawdownCriterion();



    public static Flux<? extends StrategyResult> createStorableResultsForStrategy(Strategies strategy, BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return switch (strategy) {
            case ADXStrategy -> createAdxStrategyResults(series, records, symbol);
            case CCICorrectionStrategy -> createCciCorrectionsResults(series, records, symbol);
            case GlobalExtremaStrategy -> createGlobalExtremaResults(series, records, symbol);
            case MovingAveragesStrategy -> createMovingAverageStrategyResults(series, records, symbol);
        };
    }

    private static Flux<MovingMomentumStrategyResult> createMovingMomentumStrategyResults(BaseBarSeries series, Flux<TradingRecord> records) {
        return records
                .map(record ->
                        MovingMomentumStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())


                                //Criterion results
                                .winningPositionsRatio(winningPositionsRatio.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .grossProfit(grossProfit.calculate(series, record).doubleValue())
                                .netProfit(netProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .avgProfitsvsByAndHold(avgProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .netProfitsvsByAndHold(netProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .returnOverMaxDrawdown(returnOverMaxDrawdown.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .shortSMADays(Integer.parseInt(record.getStrategyParameters().get("shortSMADays")))
                                .longSMADays(Integer.parseInt(record.getStrategyParameters().get("longSMADays")))
                                .useEMA(Boolean.parseBoolean(record.getStrategyParameters().get("useEma")))
                                .stochasticIndicatorDays(Integer.parseInt(record.getStrategyParameters().get("stochasticIndicatorDays")))
                                .stochasticIndicatorThreshold(Integer.parseInt(record.getStrategyParameters().get("stochasticIndicatorThreshold")))
                                .lowMacD(Integer.parseInt(record.getStrategyParameters().get("lowMacD")))
                                .highMacD(Integer.parseInt(record.getStrategyParameters().get("highMacD")))
                                .emaMacDDays(Integer.parseInt(record.getStrategyParameters().get("emaMacDDays")))

                                .build()
                );
    }

    private static Flux<GlobalExtremaStrategyResult> createGlobalExtremaResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .map(record ->
                        GlobalExtremaStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .winningPositionsRatio(winningPositionsRatio.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .grossProfit(grossProfit.calculate(series, record).doubleValue())
                                .netProfit(netProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .avgProfitsvsByAndHold(avgProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .netProfitsvsByAndHold(netProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .returnOverMaxDrawdown(returnOverMaxDrawdown.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .rangeInPercent(Integer.parseInt(record.getStrategyParameters().get("rangeInPercent")))
                                .numberOfBars(Integer.parseInt(record.getStrategyParameters().get("numberOfBars")))

                                .build()
                );
    }

    private static Flux<MovingAveragesStrategyResult> createMovingAverageStrategyResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .map(record ->
                        MovingAveragesStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .winningPositionsRatio(winningPositionsRatio.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .grossProfit(grossProfit.calculate(series, record).doubleValue())
                                .netProfit(netProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .avgProfitsvsByAndHold(avgProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .netProfitsvsByAndHold(netProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .returnOverMaxDrawdown(returnOverMaxDrawdown.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .buyShortMA(Integer.parseInt(record.getStrategyParameters().get("buyShortMA")))
                                .buyLongMA(Integer.parseInt(record.getStrategyParameters().get("buyLongMA")))
                                .sellShortMA(Integer.parseInt(record.getStrategyParameters().get("sellShortMA")))
                                .sellLongMA(Integer.parseInt(record.getStrategyParameters().get("sellLongMA")))
                                .movingAverageType(record.getStrategyParameters().get("movingAverageType"))

                                .build()
                );
    }

    private static Flux<AdxStrategyResult> createAdxStrategyResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .map(record ->
                        AdxStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .winningPositionsRatio(winningPositionsRatio.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .grossProfit(grossProfit.calculate(series, record).doubleValue())
                                .netProfit(netProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .avgProfitsvsByAndHold(avgProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .netProfitsvsByAndHold(netProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .returnOverMaxDrawdown(returnOverMaxDrawdown.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .smaDays(Integer.parseInt(record.getStrategyParameters().get("smaDays")))
                                .adxDays(Integer.parseInt(record.getStrategyParameters().get("adxDays")))
                                .overAdxIndicatorDays(Integer.parseInt(record.getStrategyParameters().get("overAdxIndicatorDays")))

                                .build()
                );
    }

    private static Flux<CciCorrectionsStrategyResult> createCciCorrectionsResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .map(record ->
                        CciCorrectionsStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .winningPositionsRatio(winningPositionsRatio.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .grossProfit(grossProfit.calculate(series, record).doubleValue())
                                .netProfit(netProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .avgProfitsvsByAndHold(avgProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .netProfitsvsByAndHold(netProfitsvsByAndHold.calculate(series, record).doubleValue())
                                .returnOverMaxDrawdown(returnOverMaxDrawdown.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .shortCci(Integer.parseInt(record.getStrategyParameters().get("shortCci")))
                                .longCci(Integer.parseInt(record.getStrategyParameters().get("longCci")))
                                .unstablePeriod(Integer.parseInt(record.getStrategyParameters().get("unstablePeriod")))

                                .build()
                );
    }
}