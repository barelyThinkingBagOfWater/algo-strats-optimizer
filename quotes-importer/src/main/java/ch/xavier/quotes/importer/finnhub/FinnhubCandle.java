package ch.xavier.quotes.importer.finnhub;

import ch.xavier.quotes.Quote;
import ch.xavier.quotes.typedQuotes.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple6;

@NoArgsConstructor //required for deserialization
@Getter
@AllArgsConstructor
@ToString
public class FinnhubCandle {

    private String symbol;
    private Double[] c; //close
    private Double[] h; //high
    private Double[] l; //low
    private Double[] o; //open
    private String s; //"ok" or "no_data"
    private Long[] t; //timestamp
    private Long[] v; //volume

    public void setSymbol(String symbol) { //to set it after receiving the candle without symbol from Finnhub
        this.symbol = symbol;
    }

    public Flux<? extends Quote> toQuotes(Quote.QuoteType type) { //consider when arrays are not the same size

        if ("no_data".equals(s) || c == null || h == null || l == null || o == null || t == null || v == null) {
            return Flux.empty();
        }

        final Flux<Double> closeFlux = Flux.fromArray(c);
        final Flux<Double> highFlux = Flux.fromArray(h);
        final Flux<Double> lowFlux = Flux.fromArray(l);
        final Flux<Double> openFlux = Flux.fromArray(o);
        final Flux<Long> timestampFlux = Flux.fromArray(t);
        final Flux<Long> volumeFlux = Flux.fromArray(v);

        Flux<Tuple6<Long, Double, Double, Double, Double, Long>> zipedFlux = Flux.zip(
                timestampFlux,
                closeFlux,
                highFlux,
                lowFlux,
                openFlux,
                volumeFlux
        );

        return switch (type) {
            case DAILY -> zipedFlux.flatMap(quote -> Flux.just(new DailyQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
            case ONE_MIN -> zipedFlux.flatMap(quote -> Flux.just(new OneMinQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
            case FIVE_MIN -> zipedFlux.flatMap(quote -> Flux.just(new FiveMinQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
            case FIFTEEN_MIN -> zipedFlux.flatMap(quote -> Flux.just(new FifteenMinQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
            case THIRTY_MIN -> zipedFlux.flatMap(quote -> Flux.just(new ThirtyMinQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
            case ONE_HOUR -> zipedFlux.flatMap(quote -> Flux.just(new HourlyQuote(symbol, quote.getT1(), quote.getT2(),
                    quote.getT3(), quote.getT4(), quote.getT5(), quote.getT6())));
        };
    }
}