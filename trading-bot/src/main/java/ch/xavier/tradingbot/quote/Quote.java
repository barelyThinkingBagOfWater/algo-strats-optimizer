package ch.xavier.tradingbot.quote;

import ch.xavier.tradingbot.quote.typed.QuoteType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

    public Bar toBar() {
        throw new IllegalAccessError("This method should never be called from here");
    }

    public Bar toBar(QuoteType type) {
        int durationInMinutes = 0;

        switch (type) {
            case ONE_MIN -> durationInMinutes = 1;
            case FIVE_MIN -> durationInMinutes = 5;
            case FIFTEEN_MIN -> durationInMinutes = 15;
            case THIRTY_MIN -> durationInMinutes = 30;
            case HOURLY -> durationInMinutes = 60;
            case DAILY -> durationInMinutes = 60 * 24;
            default -> throw new IllegalArgumentException("Quote type not yet implemented: " + type);
        }

        return BaseBar.builder(DecimalNum::valueOf, Number.class)
                .timePeriod(Duration.ofMinutes(durationInMinutes))
                .endTime(ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(this.timestamp),
                        ZoneOffset.UTC))
                .openPrice(this.open)
                .closePrice(this.close)
                .highPrice(this.high)
                .lowPrice(this.low)
                .volume(this.volume)
                .build();
    }
}
