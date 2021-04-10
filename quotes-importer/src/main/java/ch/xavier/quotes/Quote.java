package ch.xavier.quotes;

import ch.xavier.quotes.typedQuotes.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Quote {

    private final String symbol;
    private final Long timestamp;

    private final Double close;
    private final Double high;
    private final Double low;
    private final Double open;
    private final Long volume;

    public enum QuoteType {
        ONE_MIN(OneMinQuote.class), FIVE_MIN(FiveMinQuote.class), FIFTEEN_MIN(FifteenMinQuote.class),
        THIRTY_MIN(ThirtyMinQuote.class), ONE_HOUR(HourlyQuote.class), DAILY(DailyQuote.class);

        private final Class<? extends Quote> quoteClass;

        QuoteType(Class<? extends Quote> quoteClass) {
            this.quoteClass = quoteClass;
        }

        public Class getQuoteClass() {
            return this.quoteClass;
        }
    }
}
