package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.Quote;
import ch.xavier.quotes.Quote.QuoteType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class FinnhubAdapter {

    private final WebClient webClient;

    private static final int CALLS_ALLOWED_PER_SECOND = 1;
    private static final int SIXTEEN_MB_BUFFER = 16 * 1024 * 1024;


    public FinnhubAdapter() {
        webClient = WebClient.builder()
                .baseUrl(FinnhubUriFactory.getRestBaseStockUrl())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configuration -> configuration
                                .defaultCodecs()
                                .maxInMemorySize(SIXTEEN_MB_BUFFER))
                        .build())
                .build();
    }


    public Flux<Quote> getQuotes(final Flux<String> symbols, QuoteType type) {
        return getCandlesFromUris(FinnhubUriFactory.getQuotesUri(symbols, type))
                .flatMap(candle -> candle.toQuotes(type));
    }

    public Flux<Quote> getQuotes(final String symbol, QuoteType type) {
        return getCandlesFromUris(FinnhubUriFactory.getQuotesUri(Flux.just(symbol), type))
                .flatMap(candle -> candle.toQuotes(type));
    }


    private Flux<FinnhubCandle> getCandlesFromUris(final Flux<String> uris) {
        return uris
                .delayElements(Duration.ofMillis((1 / CALLS_ALLOWED_PER_SECOND) * 1000))
                .doOnNext(uri -> log.debug("Calling uri:{}", uri.split("&token")[0]))
                .flatMap(uri -> webClient
                        .get()
                        .uri(uri)
                        .exchangeToMono(response -> response.bodyToMono(FinnhubCandle.class))
                        .doOnNext(candle -> candle.setSymbol(extractSymbolFromUrl(uri)))
                        .retry()
                );
    }

    private String extractSymbolFromUrl(String url) {
        return url.split("&resolution")[0].split("symbol=")[1];
    }

    public Flux<String> getCryptoSymbolsForBinance() {
        return webClient
                .get()
                .uri(FinnhubUriFactory.getListCryptoSymbolsBinanceUrl())
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(FinnhubCryptoSymbol.class))
                .map(FinnhubCryptoSymbol::getSymbol);
    }
}
