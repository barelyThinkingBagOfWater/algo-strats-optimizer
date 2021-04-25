package ch.xavier.tradingbot.api;

import net.jacobpeterson.alpaca.enums.order.OrderSide;
import org.ta4j.core.num.Num;

public record CreateOrderMessage(String symbol, OrderSide orderSide, Num lastPrice) {
}
