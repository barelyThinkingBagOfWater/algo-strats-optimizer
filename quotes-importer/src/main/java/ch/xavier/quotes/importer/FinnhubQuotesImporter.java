package ch.xavier.quotes.importer;

import ch.xavier.quotes.MongoQuotesRepository;
import ch.xavier.quotes.Quote;
import ch.xavier.quotes.Quote.QuoteType;
import ch.xavier.quotes.importer.finnhub.FinnhubAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class FinnhubQuotesImporter {

    private final FinnhubAdapter finnhubAdapter;
    private final MongoQuotesRepository quotesRepository;

    @Autowired
    public FinnhubQuotesImporter(FinnhubAdapter finnhubAdapter, MongoQuotesRepository quotesRepository) {
        this.finnhubAdapter = finnhubAdapter;
        this.quotesRepository = quotesRepository;
    }


    public Flux<Quote> fetchAndSaveQuotes(Flux<String> symbols, QuoteType type) {
        return finnhubAdapter.getQuotes(symbols, type)
                //TEST ME and use me if needed, goes after symbols above
//                .filter(symbol -> quotesRepository.findAllBySymbol(symbol, type.getQuoteClass()).count()
//                        .block().equals(0)), type)
                .flatMap(quotesRepository::save);
    }

    public Flux<Quote> fetchAndSaveQuotes(String symbol, QuoteType type) {
        return finnhubAdapter.getQuotes(symbol, type)
                .flatMap(quotesRepository::save);
    }

    public Flux<Quote> fetchAndSaveQuotesOfAllTypes(Flux<String> symbols) {
        return fetchAndSaveQuotes(symbols, QuoteType.ONE_MIN)
                .thenMany(fetchAndSaveQuotes(symbols, QuoteType.FIVE_MIN))
                .thenMany(fetchAndSaveQuotes(symbols, QuoteType.FIFTEEN_MIN))
                .thenMany(fetchAndSaveQuotes(symbols, QuoteType.THIRTY_MIN))
                .thenMany(fetchAndSaveQuotes(symbols, QuoteType.ONE_HOUR))
                .thenMany(fetchAndSaveQuotes(symbols, QuoteType.DAILY));
    }
}
