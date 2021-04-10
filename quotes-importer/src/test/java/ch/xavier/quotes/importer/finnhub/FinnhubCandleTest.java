package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.Quote;
import ch.xavier.quotes.typedQuotes.DailyQuote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class FinnhubCandleTest {


    @Test
    public void toQuotes_worksAsExpected() {
        // GIVEN
        final String symbol = "SYMBOL";
        final Long timestamp1 = 1L;
        final Long timestamp2 = 2L;
        final Double high1 = 4.0;
        final Double high2 = 5.0;

        final Double close = 1.0;
        final Double open = 2.0;
        final Double low = 3.0;
        final Long volume = 3L;

        FinnhubCandle candle = new FinnhubCandle(
                symbol,
                new Double[]{close, close},
                new Double[]{high1, high2},
                new Double[]{low, low},
                new Double[]{open, open},
                "ok",
                new Long[]{timestamp1, timestamp2},
                new Long[]{volume, volume});

        // WHEN
        List<? extends Quote> quotes = candle.toQuotes(Quote.QuoteType.DAILY).collectList().block();
        DailyQuote quote1 = (DailyQuote) quotes.get(0);
        DailyQuote quote2 = (DailyQuote) quotes.get(1);

        // THEN
        Assertions.assertEquals(quote1.getOpen(), open);
        Assertions.assertEquals(quote2.getOpen(), open);
        Assertions.assertEquals(quote1.getVolume(), volume);
        Assertions.assertEquals(quote2.getVolume(), volume);
        Assertions.assertEquals(quote1.getLow(), low);
        Assertions.assertEquals(quote2.getLow(), low);
        Assertions.assertEquals(quote1.getClose(), close);
        Assertions.assertEquals(quote2.getClose(), close);

        Assertions.assertEquals(quote1.getHigh(), high1);
        Assertions.assertEquals(quote2.getHigh(), high2);
        Assertions.assertEquals(quote1.getTimestamp(), timestamp1);
        Assertions.assertEquals(quote2.getTimestamp(), timestamp2);
    }
}