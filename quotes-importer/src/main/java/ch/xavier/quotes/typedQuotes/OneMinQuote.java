package ch.xavier.quotes.typedQuotes;

import ch.xavier.quotes.Quote;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "one_min_quotes")
public class OneMinQuote extends Quote {

    public OneMinQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }
}
