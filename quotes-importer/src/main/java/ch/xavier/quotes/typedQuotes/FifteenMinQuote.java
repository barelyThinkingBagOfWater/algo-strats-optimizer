package ch.xavier.quotes.typedQuotes;

import ch.xavier.quotes.Quote;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fifteen_min_quotes")
public class FifteenMinQuote extends Quote {

    public FifteenMinQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }
}
