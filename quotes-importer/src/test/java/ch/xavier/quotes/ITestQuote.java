package ch.xavier.quotes;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ITestQuote")
public class ITestQuote extends Quote {

    public ITestQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }
}