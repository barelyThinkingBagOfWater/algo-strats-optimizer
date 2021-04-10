package ch.xavier.quotes.importer.config;

import ch.xavier.quotes.Quote.QuoteType;
import ch.xavier.quotes.importer.finnhub.FinnhubUriFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
class FinnhubUriFactoryTest {

//    @Test
    public void test_daily_url() {
        // GIVEN
        Flux<String> symbols = Flux.just("FB", "MSFT");

        FinnhubUriFactory.getQuotesUri(symbols, QuoteType.DAILY)
                .doOnNext(uri -> log.info("{}", uri))
                .blockLast();

        // WHEN, you can use withVirtualTime to check the uris
//        StepVerifier
//                .create(FinnhubUriFactory.getQuotesUri(symbols, QUOTE_TYPE.DAILY))
//
//                // THEN
//                .expectNextMatches(uri -> uri.startsWith("?symbol=FB&resolution=D&from=1353392741&to="))
//                .expectNextMatches(uri -> uri.startsWith("?symbol=MSFT&resolution=D&from=1353392830&to="))
//                .verifyComplete();
    }

//    @Test
    public void test_5min_urls() {
        // GIVEN
        Flux<String> symbols = Flux.just("FB", "MSFT");

        // WHEN
        FinnhubUriFactory.getQuotesUri(symbols, QuoteType.FIVE_MIN)
                .doOnNext(uri -> log.info("{}", uri))
                .blockLast();
    }
}