package ch.xavier.tradingbot.realtime;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.DomainType;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BinanceRealtimeQuotesImporter extends AbstractBehavior<WatchSymbolMessage> {

    private final Map<String, Closeable> realtimeQuotesImporters = new HashMap<>();
    private static BinanceApiWebSocketClient websocketClient;
    private static ActorRef<Topic.Command<NewBarMessage>> topicRef;


    public static Behavior<WatchSymbolMessage> create(BinanceApiClientFactory binanceApi,
                                                      ActorRef<Topic.Command<NewBarMessage>> newQuotesTopicActorRef) {
        websocketClient = binanceApi.newWebSocketClient(DomainType.Com);
        topicRef = newQuotesTopicActorRef;

        return Behaviors.setup(BinanceRealtimeQuotesImporter::new);
    }

    private BinanceRealtimeQuotesImporter(ActorContext<WatchSymbolMessage> context) {
        super(context);
    }

    @Override
    public Receive<WatchSymbolMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(WatchSymbolMessage.class, this::watchSymbol)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WatchSymbolMessage> watchSymbol(WatchSymbolMessage message) {
        getContext().getLog().info("Now watching symbol:{} from Binance", message.symbol());

        Closeable closeable = websocketClient.onCandlestickEvent(message.symbol(), CandlestickInterval.ONE_MINUTE,
                response -> getContext().getLog().info("New event received for symbol:{}, {}", message.symbol(), response));

        realtimeQuotesImporters.put(message.symbol(), closeable);

        return this;
    }

    private Behavior<WatchSymbolMessage> onPostStop() { //Test me in real conditions
        getContext().getSystem().log().info("Stopping quotesImporter, {} listeners to stop", realtimeQuotesImporters.size());

        realtimeQuotesImporters.forEach((symbol, listener) -> {
            try {
                listener.close();
            } catch (IOException e) {
                getContext().getSystem().log().error("Error when closing realtime listener for symbol:{}", symbol);
                e.printStackTrace();
            }
        });
        return this;
    }
}
