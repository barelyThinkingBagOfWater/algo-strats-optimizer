package ch.xavier.tradingbot.quote.typed;

import ch.xavier.tradingbot.quote.Quote;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ta4j.core.Bar;

@CompoundIndex(name = "fifteen_min_quote_id", def = "{'symbol' : 1, 'timestamp' : 1}, { unique: true }")
@Document(collection = "fifteen_min_quotes")
public class FifteenMinQuote extends Quote {

    public FifteenMinQuote(String symbol, Long timestamp, Double close, Double high, Double low, Double open, Long volume) {
        super(symbol, timestamp, close, high, low, open, volume);
    }

    @Override
    public Bar toBar() {
        return super.toBar(QuoteType.FIFTEEN_MIN);
    }
}
