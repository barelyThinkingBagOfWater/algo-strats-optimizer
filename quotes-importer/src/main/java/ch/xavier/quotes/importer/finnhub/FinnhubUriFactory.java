package ch.xavier.quotes.importer.finnhub;


import ch.xavier.quotes.Quote.QuoteType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@Slf4j
public class FinnhubUriFactory {

    private FinnhubUriFactory() {
    }

    //External conf/secrets
    private static final String TOKEN = "brotd4vrh5r8qo23fdmg"; //Hello Github!
    private static final String REST_BASE_URL = "https://finnhub.io/api/v1/";
    private static final String STOCK_CANDLE_URL = "stock/candle";
    private static final String CRYPTO_CANDLE_URL = "crypto/candle";
    private static final String WSS_BASE_URL = "wss://ws.finnhub.io?token=";

    private static final int EARLIEST_YEAR_FOR_QUOTES = 2012;
    private static final String DAILY_DURATION = "D";
    private static final String FIVE_MIN_DURATION = "5";
    private static final String ONE_MIN_DURATION = "1";
    private static final String FIFTEEN_MIN_DURATION = "15";
    private static final String THIRTY_MIN_DURATION = "30";
    private static final String ONE_HOUR_DURATION = "60";
    private static final String CRYPTO_PREFIX_SYMBOL = "BINANCE";

    private static final long DAILY_INTERVAL = 243_734_400; //slightly less than 8 years
    private static final long COMMON_INTERVAL = 15_500_000;

    private static final Calendar EARLIEST_DATE_FOR_QUOTES = new Calendar.Builder().setDate(
            EARLIEST_YEAR_FOR_QUOTES, Calendar.JANUARY, 1
    ).build();


    public static Flux<String> getQuotesUri(final Flux<String> symbols, QuoteType type) {
        return switch (type) {
            case DAILY -> getDailyQuotesUri(symbols);
            case ONE_MIN -> getNonDailyQuotesUri(symbols, ONE_MIN_DURATION);
            case FIVE_MIN -> getNonDailyQuotesUri(symbols, FIVE_MIN_DURATION);
            case FIFTEEN_MIN -> getNonDailyQuotesUri(symbols, FIFTEEN_MIN_DURATION);
            case THIRTY_MIN -> getNonDailyQuotesUri(symbols, THIRTY_MIN_DURATION);
            case ONE_HOUR -> getNonDailyQuotesUri(symbols, ONE_HOUR_DURATION);
        };
    }

    private static Flux<String> getDailyQuotesUri(final Flux<String> symbols) {
        return getQuotesUri(symbols, DAILY_INTERVAL, DAILY_DURATION);
    }

    private static Flux<String> getNonDailyQuotesUri(final Flux<String> symbols, final String duration) {
        return getQuotesUri(symbols, COMMON_INTERVAL, duration);
    }

    private static Flux<String> getQuotesUri(final Flux<String> symbols, final long interval, final String duration) {
        final List<String> uris = new ArrayList<>();
        long lastTimestamp = System.currentTimeMillis() / 1000;

        //Not terrific but does the job. Will be improved when needed.
        for (long i = lastTimestamp - interval;
             i > EARLIEST_DATE_FOR_QUOTES.getTimeInMillis() / 1000;
             i -= interval) {

            final long currentLastTimestamp = lastTimestamp;
            final long currentI = i;

            symbols.subscribe(symbol -> uris.add(getURIForQuotes(symbol, duration, currentI, currentLastTimestamp)));
            lastTimestamp = i;
        }

        return Flux.fromStream(uris.stream());
    }

    private static String getURIForQuotes(final String symbol, final String duration, final long from, final long to) {
        String uri= (symbol.startsWith(CRYPTO_PREFIX_SYMBOL)) ? CRYPTO_CANDLE_URL : STOCK_CANDLE_URL;

        return uri + "?symbol=" + symbol + "&resolution=" + duration + "&from="
                + from + "&to=" + to + "&token=" + TOKEN;
    }

    public static String getRestBaseStockUrl() {
        return REST_BASE_URL;
    }

    public static String getWssBaseUrl() {
        return WSS_BASE_URL.concat(TOKEN);
    }
}
