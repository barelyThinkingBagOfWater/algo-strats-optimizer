package ch.xavier.tradingbot.realtime;

import org.ta4j.core.Bar;

public record NewBarMessage(Bar bar, String symbol) {
}
