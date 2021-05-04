package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.importer.SymbolsRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;

/**
 * Move this logic to FinnhubQuotesImporter?
 */
@Slf4j
//@Component
public class FinnhubRealTimeQuotesImporter implements AutoCloseable {

    private final static URI WSS_URI = URI.create(FinnhubUriFactory.getWssBaseUrl());
    private final ReactorNettyWebSocketClient webSocketClient = new ReactorNettyWebSocketClient();

    //replace me with real class in correct package or at least inner class?
    private final static String SUBSCRIPTION_TEMPLATE = "{\"type\":\"subscribe\",\"symbol\":\"%SYMBOL\"}";


    public FinnhubRealTimeQuotesImporter() {
        log.info("Now entering ctor of RealTime quotes importer");

        Flux<String> symbolsToWatchMessage = SymbolsRegistry.TWO_OFTEN_TRADED_SYMBOLS
                .map(symbol -> SUBSCRIPTION_TEMPLATE.replace("%SYMBOL", symbol));


        //TODO: IF NO ANSWER, NO PRICE COMING! WATCH OUT and test only when you know there are trades coming
        webSocketClient.execute(WSS_URI, session ->
                session.receive()
                        .doOnNext(receivedMessage -> log.info("message received:{}", receivedMessage))
                        .then())
                .subscribe();
    }


    private void OLD_establishWssConnection(Flux<String> symbolsToWatchMessage) {
        webSocketClient.execute(WSS_URI, session ->
                session.send(symbolsToWatchMessage
                        .map(session::textMessage))
                        .thenMany(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .doOnNext(message -> log.info("Message received:{}", message))
                                .doOnSubscribe(subscriber -> log.info("Connection to Finnhub WSS established"))
                                .doOnError(e -> log.error("Error when consuming message:", e))
                        )
                        .then())
                .subscribe(what -> log.info("Connection opened and processing has begun I surmise? Current object:{}", what));
    }

    @Override
    public void close() {
        webSocketClient.execute(WSS_URI, WebSocketSession::close)
                .subscribe(i -> log.info("Finnhub WSS connection closed, current object:{}", i));
    }
}
