package ch.xavier.tradingbot;

import akka.actor.ActorRef;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.quote.Quote;
import ch.xavier.tradingbot.quote.typed.QuoteType;
import ch.xavier.tradingbot.strategies.RunnableStrategy;
import ch.xavier.tradingbot.strategies.specific.GlobalExtremaStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TradingBot {

    private final Map<String, Set<ActorRef>> runningStrategies = new HashMap<>();

    private final MongoQuotesRepository quotesRepository;


    @Autowired
    public TradingBot(MongoQuotesRepository quotesRepository) {
        this.quotesRepository = quotesRepository;

        BaseBarSeries series = createSeriesForSymbol("FB");

        GlobalExtremaStrategy strategy1 = new GlobalExtremaStrategy(7, 7);
        GlobalExtremaStrategy strategy2 = new GlobalExtremaStrategy(7, 2016);
    }


    private void runStrategy(GlobalExtremaStrategy strategy, BaseBarSeries series) {
        //To avoid MemoryOverflow, specific to globalExtrema, see later how you want to manage that
        series.setMaximumBarCount(strategy.numberOfBars() + 1);

        //TODO: Create here the actor and add into it the method to process the new Bar from a message
        //For now one serie per actor as well so each is self-contained, see later for improvment
        //Then run it on AWS for a night and check logs for buy/sell signals?
    }

    private BaseBarSeries createSeriesForSymbol(String symbol) {
        final BaseBarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();

        quotesRepository
                .findAllBySymbol("FB", QuoteType.ONE_MIN)
                .map(Quote::toBar)
                .doOnNext(series::addBar)
                .blockLast();

        return series;
    }
}
