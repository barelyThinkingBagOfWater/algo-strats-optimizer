package ch.xavier.tradingbot.strategies;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.quote.Quote;
import ch.xavier.tradingbot.quote.typed.QuoteType;
import ch.xavier.tradingbot.realtime.NewBarMessage;
import ch.xavier.tradingbot.realtime.RealtimeQuotesImporter;
import ch.xavier.tradingbot.realtime.WatchSymbolMessage;
import ch.xavier.tradingbot.strategies.specific.GlobalExtremaStrategy;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.apache.commons.lang.SerializationUtils;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class StrategiesInitializer {

    private final static Map<String, BarSeries> filledSeries = new HashMap<>();

    private static ActorRef<WatchSymbolMessage> quotesImporterActorRef;
    private static ActorRef<Topic.Command<NewBarMessage>> newQuotesTopicActorRef;
    private static MongoQuotesRepository quotesRepository;


    public static Behavior<Void> create(MongoQuotesRepository repository, AlpacaAPI api) {
        quotesRepository = repository;

        GlobalExtremaStrategy strategy1 = new GlobalExtremaStrategy(7, 7);
        GlobalExtremaStrategy strategy2 = new GlobalExtremaStrategy(7, 2016);

        return Behaviors.setup(
                context -> {
                    context.getLog().info("Creating importer of realtime quotes, the pub/sub topic to propagate them");
                    newQuotesTopicActorRef = context.spawn(Topic.create(NewBarMessage.class, "realtimeQuoteTopic"),
                                    "realtimeQuoteTopic");
                    quotesImporterActorRef =
                            context.spawn(RealtimeQuotesImporter.create(api, newQuotesTopicActorRef), "realTimeQuotesImporter");

                    context.getLog().info("Initializing symbols to watch");
                    initializeSymbol("FB");

                    context.getLog().info("Creating running strategies actors");
                    runStrategyOnSymbol(strategy1, "FB", context);
                    runStrategyOnSymbol(strategy2, "FB", context);

                    Thread.sleep(5000);

                    //TEMP TEST
                    context.getLog().info("Sending fake message to running strats actors");
                    Bar tempBar = filledSeries.get("FB").getLastBar();
                    BaseBar baseBar = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now(), tempBar.getOpenPrice(),
                            tempBar.getHighPrice(), tempBar.getLowPrice(), tempBar.getClosePrice(), tempBar.getVolume(),
                            tempBar.getAmount());
                    newQuotesTopicActorRef.tell(Topic.publish(new NewBarMessage(baseBar, "FB")));


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

        quotesImporterActorRef.tell(new WatchSymbolMessage(symbol));
    }

    private static void runStrategyOnSymbol(GlobalExtremaStrategy strategy, String symbol, ActorContext context) {
        BarSeries series = filledSeries.get(symbol);
        series.setMaximumBarCount(strategy.numberOfBars() + 1);

        ActorRef strategyActorRef = context.spawn(RunningStrategyActor.create(strategy, series), strategy.getNameForActor());

        newQuotesTopicActorRef.tell(Topic.subscribe(strategyActorRef));
    }


}
