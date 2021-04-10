package ch.xavier.quotes.typedQuotes;

import ch.xavier.quotes.Quote;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "hourly_quotes")
public class HourlyQuote extends Quote {

    public HourlyQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }
}
