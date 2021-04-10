package ch.xavier.quotes.importer;

import ch.xavier.quotes.MongoQuotesRepository;
import ch.xavier.quotes.Quote;
import ch.xavier.quotes.Quote.QuoteType;
import ch.xavier.quotes.importer.finnhub.FinnhubQuotesImporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class QuotesImporter {

    private final FinnhubQuotesImporter finnhubQuotesImporter;
    private final MongoQuotesRepository quotesRepository;

    @Autowired
    public QuotesImporter(FinnhubQuotesImporter finnhubQuotesImporter, MongoQuotesRepository quotesRepository) {
        this.finnhubQuotesImporter = finnhubQuotesImporter;
        this.quotesRepository = quotesRepository;
    }


    public Flux<Quote> fetchAndSaveQuotes(Flux<String> symbols, QuoteType type) {
        return finnhubQuotesImporter.getQuotes(symbols, type)
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
