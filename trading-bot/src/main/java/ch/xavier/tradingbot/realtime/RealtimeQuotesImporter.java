package ch.xavier.tradingbot.realtime;

import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.abstracts.websocket.exception.WebsocketException;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.websocket.marketdata.listener.MarketDataListener;
import net.jacobpeterson.alpaca.websocket.marketdata.listener.MarketDataListenerAdapter;
import net.jacobpeterson.alpaca.websocket.marketdata.message.MarketDataMessageType;
import net.jacobpeterson.domain.alpaca.marketdata.realtime.MarketDataMessage;
import net.jacobpeterson.domain.alpaca.marketdata.realtime.bar.BarMessage;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RealtimeQuotesImporter {

    private final AlpacaAPI api = new AlpacaAPI();

    private final Map<String, MarketDataListener> realtimeQuotesImporters = new HashMap<>();


    public void watchSymbol(String symbol) {
        MarketDataListener listenerTSLA = new MarketDataListenerAdapter(symbol, MarketDataMessageType.BAR){
            @Override
            public void onStreamUpdate(MarketDataMessageType streamMessageType, MarketDataMessage streamMessage) {
                if (streamMessageType == MarketDataMessageType.BAR) {
                    BarMessage barMessage = (BarMessage) streamMessage;
                        log.info("Bar: Open={} High={} Low={} Close={} Timestamp={}",
                            barMessage.getOpen(),
                            barMessage.getHigh(),
                            barMessage.getLow(),
                            barMessage.getClose(),
                            barMessage.getTimestamp());
                } else {
                    log.error("Unknowm message received when watching symbol:{}, here it is:{}", symbol,
                            streamMessage.toString());
                }
            }
        };
//        realtimeQuotesImporters.put(symbol, listenerTSLA);

        try {
            api.addMarketDataStreamListener(listenerTSLA);
        } catch (WebsocketException e) {
            log.error("Error when adding listener for symbol:{}", symbol);
            e.printStackTrace();
        }
    }

    public Map<String, MarketDataListener> getRealtimeQuotesImporters() {
        return realtimeQuotesImporters;
    }


    @PreDestroy
    public void close() {
        realtimeQuotesImporters.forEach((symbol, listener) -> {
            try {
                api.removeMarketDataStreamListener(listener);
            } catch (WebsocketException e) {
                log.error("Error when closing realtime listener for symbol:{}", symbol);
                e.printStackTrace();
            }
        });
    }
}
