package ch.xavier.backtester.result;

import ch.xavier.backtester.result.specific.*;
import ch.xavier.backtester.strategy.Strategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class ResultsFactory {

    //Criterion
    private static  final AverageProfitableTradesCriterion avgProfitTrades = new AverageProfitableTradesCriterion();
    private static final AverageProfitCriterion avgProfit = new AverageProfitCriterion();
    private static final TotalProfitCriterion totalProfit = new TotalProfitCriterion();
    private static final BuyAndHoldCriterion buyAndHold = new BuyAndHoldCriterion();
    private static final NumberOfTradesCriterion numTrades = new NumberOfTradesCriterion();
    private static final VersusBuyAndHoldCriterion vsByAndHold = new VersusBuyAndHoldCriterion(avgProfit);
    private static final RewardRiskRatioCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
    private static final MaximumDrawdownCriterion maxDrawDown = new MaximumDrawdownCriterion();


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
                .publishOn(Schedulers.parallel())
                .map(record ->
                        MovingMomentumStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())


                                //Criterion results
                                .avgProfitTrades(avgProfitTrades.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .totalProfit(totalProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .vsByAndHold(vsByAndHold.calculate(series, record).doubleValue())
                                .rewardRiskRatio(rewardRiskRatio.calculate(series, record).doubleValue())
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
                .publishOn(Schedulers.parallel())
                .map(record ->
                        GlobalExtremaStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .avgProfitTrades(avgProfitTrades.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .totalProfit(totalProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .vsByAndHold(vsByAndHold.calculate(series, record).doubleValue())
                                .rewardRiskRatio(rewardRiskRatio.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .rangeInPercent(Integer.parseInt(record.getStrategyParameters().get("rangeInPercent")))
                                .numberOfBars(Integer.parseInt(record.getStrategyParameters().get("numberOfBars")))

                                .build()
                );
    }

    private static Flux<MovingAveragesStrategyResult> createMovingAverageStrategyResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .publishOn(Schedulers.parallel())
                .map(record ->
                        MovingAveragesStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .avgProfitTrades(avgProfitTrades.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .totalProfit(totalProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .vsByAndHold(vsByAndHold.calculate(series, record).doubleValue())
                                .rewardRiskRatio(rewardRiskRatio.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .movingAverageType(record.getStrategyParameters().get("movingAverageType"))
                                .buyShortMA(Integer.parseInt(record.getStrategyParameters().get("buyShortMA")))
                                .buyLongMA(Integer.parseInt(record.getStrategyParameters().get("buyLongMA")))
                                .sellShortMA(Integer.parseInt(record.getStrategyParameters().get("sellShortMA")))
                                .sellLongMA(Integer.parseInt(record.getStrategyParameters().get("sellLongMA")))

                                .build()
                );
    }

    private static Flux<AdxStrategyResult> createAdxStrategyResults(BaseBarSeries series, Flux<TradingRecord> records, String symbol) {
        return records
                .publishOn(Schedulers.parallel())
                .map(record ->
                        AdxStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .avgProfitTrades(avgProfitTrades.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .totalProfit(totalProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .vsByAndHold(vsByAndHold.calculate(series, record).doubleValue())
                                .rewardRiskRatio(rewardRiskRatio.calculate(series, record).doubleValue())
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
                .publishOn(Schedulers.parallel())
                .map(record ->
                        CciCorrectionsStrategyResult.builder()
                                .strategyName(record.getCurrentStrategyName())
                                .symbol(symbol)

                                //Criterion results
                                .avgProfitTrades(avgProfitTrades.calculate(series, record).doubleValue())
                                .avgProfit(avgProfit.calculate(series, record).doubleValue())
                                .totalProfit(totalProfit.calculate(series, record).doubleValue())
                                .buyAndHold(buyAndHold.calculate(series, record).doubleValue())
                                .numTrades(numTrades.calculate(series, record).doubleValue())
                                .vsByAndHold(vsByAndHold.calculate(series, record).doubleValue())
                                .rewardRiskRatio(rewardRiskRatio.calculate(series, record).doubleValue())
                                .maxDrawDown(maxDrawDown.calculate(series, record).doubleValue())

                                //Strategy parameters
                                .shortCci(Integer.parseInt(record.getStrategyParameters().get("shortCci")))
                                .longCci(Integer.parseInt(record.getStrategyParameters().get("longCci")))
                                .unstablePeriod(Integer.parseInt(record.getStrategyParameters().get("unstablePeriod")))

                                .build()
                );
    }
}