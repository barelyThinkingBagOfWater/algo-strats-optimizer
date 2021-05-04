package ch.xavier.tradingbot.realtime;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import ch.xavier.tradingbot.strategies.RunningStrategyActor;
import net.jacobpeterson.abstracts.websocket.exception.WebsocketException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.websocket.marketdata.listener.MarketDataListener;
import net.jacobpeterson.alpaca.websocket.marketdata.listener.MarketDataListenerAdapter;
import net.jacobpeterson.alpaca.websocket.marketdata.message.MarketDataMessageType;
import net.jacobpeterson.domain.alpaca.marketdata.realtime.MarketDataMessage;
import net.jacobpeterson.domain.alpaca.marketdata.realtime.bar.BarMessage;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class RealtimeQuotesImporter extends AbstractBehavior<WatchSymbolMessage> {

    private final Map<String, MarketDataListener> realtimeQuotesImporters = new HashMap<>();
    private static AlpacaAPI api;
    private static ActorRef<Topic.Command<NewBarMessage>> topicRef;


    public static Behavior<WatchSymbolMessage> create(AlpacaAPI alpacaAPI, ActorRef<Topic.Command<NewBarMessage>> newQuotesTopicActorRef) {
        api = alpacaAPI;
        topicRef = newQuotesTopicActorRef;

        return Behaviors.setup(RealtimeQuotesImporter::new);
    }

    private RealtimeQuotesImporter(ActorContext<WatchSymbolMessage> context) {
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
        getContext().getLog().info("Now watching symbol:{}", message.symbol());

        MarketDataListener listenerTSLA = new MarketDataListenerAdapter(message.symbol(), MarketDataMessageType.BAR) {
            @Override
            public void onStreamUpdate(MarketDataMessageType streamMessageType, MarketDataMessage streamMessage) {
                if (streamMessageType == MarketDataMessageType.BAR) {
                    BarMessage barMessage = (BarMessage) streamMessage;
                    getContext().getSystem().log().info("Bar received for symbol:{}: Open={} High={} Low={} Close={} " +
                                    "Timestamp={}, sending quote to topic",
                            message.symbol(),
                            barMessage.getOpen(),
                            barMessage.getHigh(),
                            barMessage.getLow(),
                            barMessage.getClose(),
                            barMessage.getTimestamp());

                    topicRef.tell(Topic.publish(new NewBarMessage(
                            BaseBar.builder()
                                    .openPrice(DecimalNum.valueOf(barMessage.getOpen()))
                                    .closePrice(DecimalNum.valueOf(barMessage.getClose()))
                                    .highPrice(DecimalNum.valueOf(barMessage.getHigh()))
                                    .lowPrice(DecimalNum.valueOf(barMessage.getLow()))
                                    .endTime(barMessage.getTimestamp())
                                    .volume(DecimalNum.valueOf(barMessage.getVolume()))
                                    .build(),
                            message.symbol())));
                } else {
                    getContext().getSystem().log().error("Unknowm message received when watching symbol:{}, here it is:{}", message.symbol(),
                            streamMessage.toString());
                }
            }
        };

        realtimeQuotesImporters.put(message.symbol(), listenerTSLA);

        try {
            api.addMarketDataStreamListener(listenerTSLA);
        } catch (WebsocketException e) {
            getContext().getSystem().log().error("Error when adding listener for symbol:{}", message.symbol());
            e.printStackTrace();
        }

        return this;
    }

    private Behavior<WatchSymbolMessage> onPostStop() { //Test me in real conditions
        getContext().getSystem().log().info("Stopping quotesImporter, {} listeners to stop", realtimeQuotesImporters.size());

        realtimeQuotesImporters.forEach((symbol, listener) -> {
            try {
                api.removeMarketDataStreamListener(listener);
            } catch (WebsocketException e) {
                getContext().getSystem().log().error("Error when closing realtime listener for symbol:{}", symbol);
                e.printStackTrace();
            }
        });
        return this;
    }
}
