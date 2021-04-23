package ch.xavier.tradingbot.strategies;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.quote.Quote;
import ch.xavier.tradingbot.quote.typed.QuoteType;
import ch.xavier.tradingbot.realtime.NewBarMessage;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Strategy;

public class RunningStrategyActor extends AbstractBehavior<NewBarMessage> {

    private final Strategy strategy;
    private final BarSeries series;
    private static MongoQuotesRepository quotesRepository;

    public static Behavior<NewBarMessage> create(RunnableStrategy strategy, MongoQuotesRepository repository) {
        quotesRepository = repository;
        return Behaviors.setup(context -> new RunningStrategyActor(context, strategy));
    }

    private RunningStrategyActor(ActorContext<NewBarMessage> context, RunnableStrategy runnableStrategy) {
        super(context);
        this.series = fetchSeries("FB");
        this.strategy = runnableStrategy.buildStrategy(series);

        getContext().getLog().info("Strategy:{} with parameters:{} is running in dedicated actor for symbol:{}",
                strategy.getName(), strategy.getParameters(), series.getName());
    }


    @Override
    public Receive<NewBarMessage> createReceive() {
        return newReceiveBuilder().onMessage(NewBarMessage.class, this::onReceive).build();
    }

    private Behavior<NewBarMessage> onReceive(NewBarMessage message) {
        getContext().getLog().info("Received message:{}", message);

        if (series.getName().equals(message.symbol())) {
            series.addBar(message.bar());
            int endIndex = series.getEndIndex();

            if (strategy.shouldEnter(endIndex)) {
                getContext().getLog().info("strategy should enter, buying stock");
            } else if (strategy.shouldExit(endIndex)) {
                getContext().getLog().info("strategy should exit, selling stock");
            }
        } else {
            getContext().getLog().info("Symbol doesn't match the current strategy, ignoring message");
        }

        return this;
    }

    public BarSeries fetchSeries(String symbol) {
        final BaseBarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();
        quotesRepository
                .findAllBySymbol(symbol, QuoteType.ONE_MIN)
                .map(Quote::toBar)
                .doOnNext(series::addBar)
                .blockLast();

//        series.setMaximumBarCount(); to avoid maxMemoryError, set me correctly

        return series;
    }
}
