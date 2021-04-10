package ch.xavier.backtester.quote.typed;

import ch.xavier.backtester.quote.Quote;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ta4j.core.Bar;

@CompoundIndex(name = "one_min_quote_id", def = "{'symbol' : 1, 'timestamp' : 1}, { unique: true }")
@Document(collection = "one_min_quotes")
public class OneMinQuote extends Quote {

    public OneMinQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }

    @Override
    public Bar toBar() {
        return super.toBar(QuoteType.ONE_MIN);
    }
}
