package ch.xavier.tradingbot.quote.typed;


import ch.xavier.tradingbot.quote.Quote;

public enum QuoteType {
    ONE_MIN(OneMinQuote.class), FIVE_MIN(FiveMinQuote.class), FIFTEEN_MIN(FifteenMinQuote.class),
    THIRTY_MIN(ThirtyMinQuote.class), HOURLY(HourlyQuote.class), DAILY(DailyQuote.class);

    private final Class<? extends Quote> quoteClass;

    QuoteType(Class<? extends Quote> quoteClass) {
        this.quoteClass = quoteClass;
    }

    public Class<? extends Quote> getQuoteClass() {
        return this.quoteClass;
    }
}

