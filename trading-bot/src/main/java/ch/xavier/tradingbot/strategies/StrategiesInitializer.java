package ch.xavier.tradingbot.strategies;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.quote.Quote;
import ch.xavier.tradingbot.quote.typed.QuoteType;
import ch.xavier.tradingbot.realtime.RealtimeQuotesImporter;
import ch.xavier.tradingbot.realtime.WatchSymbolMessage;
import ch.xavier.tradingbot.strategies.specific.GlobalExtremaStrategy;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.enums.api.DataAPIType;
import net.jacobpeterson.alpaca.enums.api.EndpointAPIType;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.*;

public class StrategiesInitializer {

    private final static Map<String, Set<ActorRef<RunningStrategyActor>>> runningStrategies = new HashMap<>();
    private final static Map<String, BarSeries> filledSeries = new HashMap<>();

    private static ActorRef<WatchSymbolMessage> quotesImporterActorRef;
    private static MongoQuotesRepository quotesRepository;


    public static Behavior<Void> create(MongoQuotesRepository repository, AlpacaAPI api) {
        quotesRepository = repository;

        GlobalExtremaStrategy strategy1 = new GlobalExtremaStrategy(7, 7);
        GlobalExtremaStrategy strategy2 = new GlobalExtremaStrategy(7, 2016);

        return Behaviors.setup(
                context -> {
                    context.getLog().info("Creating importer of Quotes and initializing symbols");
                    quotesImporterActorRef =
                            context.spawn(RealtimeQuotesImporter.create(api), "realTimeQuotesImporter");

                    initializeSymbol("FB");

                    context.getLog().info("Creating running strategies actors");

                    runStrategyOnSymbol(strategy1, "FB", context);
                    runStrategyOnSymbol(strategy2, "FB", context);

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    private static void initializeSymbol(String symbol) {

        final BaseBarSeries series = new BaseBarSeriesBuilder().withName(symbol).build();
        quotesRepository
                .findAllBySymbol(symbol, QuoteType.ONE_MIN)
                .map(Quote::toBar)
                .doOnNext(series::addBar)
                .blockLast();

        filledSeries.put(symbol, series);
        runningStrategies.put(symbol, new HashSet<>());

        quotesImporterActorRef.tell(new WatchSymbolMessage(symbol));
    }

    private static void runStrategyOnSymbol(GlobalExtremaStrategy strategy, String symbol, ActorContext context) {
        //To avoid MemoryOverflow, specific to globalExtrema, see later how you want to manage that
        BarSeries series = filledSeries.get(symbol);
        series.setMaximumBarCount(strategy.numberOfBars() + 1);

        ActorRef<RunningStrategyActor> strategyActorRef = context.spawn(RunningStrategyActor.create(strategy, series), strategy.getNameForActor());

        runningStrategies.get(symbol).add(strategyActorRef);
    }


}
