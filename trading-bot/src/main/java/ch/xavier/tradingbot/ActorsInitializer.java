package ch.xavier.tradingbot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import ch.xavier.tradingbot.api.AlpacaApiActor;
import ch.xavier.tradingbot.api.CreateOrderMessage;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.realtime.BinanceRealtimeQuotesImporter;
import ch.xavier.tradingbot.realtime.NewBarMessage;
import ch.xavier.tradingbot.realtime.AlpacaRealtimeQuotesImporter;
import ch.xavier.tradingbot.realtime.WatchSymbolMessage;
import ch.xavier.tradingbot.strategies.RunningStrategyActor;
import ch.xavier.tradingbot.strategies.specific.GlobalExtremaStrategy;
import com.binance.api.client.BinanceApiClientFactory;
import net.jacobpeterson.alpaca.AlpacaAPI;

import java.util.ArrayList;
import java.util.List;

public class ActorsInitializer {

    private static ActorRef<WatchSymbolMessage> quotesImporterActorRef;
    private static ActorRef<CreateOrderMessage> tradingApiActorRef;
    private static ActorRef<Topic.Command<NewBarMessage>> newQuotesTopicActorRef;
    private static final List<GlobalExtremaStrategy> strategies = new ArrayList<>();


    public static Behavior<Void> create(MongoQuotesRepository repository, AlpacaAPI alpacaAPI,
                                        BinanceApiClientFactory binanceApiClientFactory) {

        strategies.add(new GlobalExtremaStrategy(7, 7));
        strategies.add(new GlobalExtremaStrategy(7, 2016));

        return Behaviors.setup(
                context -> {
//                    context.getLog().info("Creating actor for trading api");
//                    tradingApiActorRef = context.spawn(AlpacaApiActor.create(api), "alpacaTradingApi");

                    context.getLog().info("Creating importer of realtime quotes and the pub/sub topic to propagate them");
                    //You could have one topic per symbol
                    newQuotesTopicActorRef = context.spawn(Topic.create(NewBarMessage.class, "realtimeQuoteTopic"),
                                    "realtimeQuoteTopic");

                    context.getLog().info("Creating binance realtime quote importer");
//                    quotesImporterActorRef = context.spawn(AlpacaRealtimeQuotesImporter.create(alpacaAPI, newQuotesTopicActorRef),
//                            "alpacaRealTimeQuotesImporter");
                    quotesImporterActorRef = context.spawn(BinanceRealtimeQuotesImporter.create(binanceApiClientFactory,
                            newQuotesTopicActorRef), "binanceRealTimeQuotesImporter");

                    //TEST
                    quotesImporterActorRef.tell(new WatchSymbolMessage("ETHBTC"));


//                    context.getLog().info("Initializing importers for symbols");
//                    initializeImporterForSymbol("FB");

//                    context.getLog().info("Creating running strategies actors");
//                    strategies.forEach(strat -> runStrategyOnSymbol(strat, repository, context));

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    private static void initializeImporterForSymbol(String symbol) {
        quotesImporterActorRef.tell(new WatchSymbolMessage(symbol));
    }

    private static void runStrategyOnSymbol(GlobalExtremaStrategy strategy, MongoQuotesRepository repository,
                                            ActorContext context) {
        ActorRef strategyActorRef = context.spawn(RunningStrategyActor.create(strategy, repository, tradingApiActorRef),
                strategy.getNameForActor());

        newQuotesTopicActorRef.tell(Topic.subscribe(strategyActorRef));
    }


}
