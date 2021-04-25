package ch.xavier.tradingbot.api;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.jacobpeterson.abstracts.enums.SortDirection;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.enums.order.OrderSide;
import net.jacobpeterson.alpaca.enums.order.OrderStatus;
import net.jacobpeterson.alpaca.enums.order.OrderTimeInForce;
import net.jacobpeterson.alpaca.rest.exception.AlpacaAPIRequestException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

public class AlpacaApiActor extends AbstractBehavior<CreateOrderMessage> {

    private static final ZonedDateTime MIN_DATE = ZonedDateTime.of(1970, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault());
    private static final int MAX_NUMBER_OF_ORDERS = 500;
    private static final int MAX_PRICE_PER_ORDER = 1000;

    private static AlpacaAPI api;

    private AlpacaApiActor(ActorContext<CreateOrderMessage> context) {
        super(context);
    }

    public static Behavior<CreateOrderMessage> create(AlpacaAPI alpacaAPI) {
        api = alpacaAPI;
        return Behaviors.setup(AlpacaApiActor::new);
    }


    @Override
    public Receive<CreateOrderMessage> createReceive() {
        return newReceiveBuilder().onMessage(CreateOrderMessage.class, this::onReceive).build();
    }

    private Behavior<CreateOrderMessage> onReceive(CreateOrderMessage message) {
        placeOrder(message.symbol(), message.orderSide(), MAX_PRICE_PER_ORDER / message.lastPrice().intValue());
        return this;
    }


    private void placeOrder(String symbol, OrderSide orderSide, int quantity) {
        try {
            if (orderSide.equals(OrderSide.BUY)) {
                if (buyOrderAlreadyExists(symbol)) {
                    getContext().getLog().info("An buy order for symbol:{} already exists, no new order will be sent",
                            symbol);
                } else {
                    getContext().getLog().info("Placing buy order for symbol:{}", symbol);
                    api.requestNewMarketOrder(symbol, quantity, OrderSide.BUY, OrderTimeInForce.DAY);
                }
            } else if (orderSide.equals(OrderSide.SELL)) {
                if (api.getOpenPositionBySymbol(symbol) != null) {
                    getContext().getLog().info("Selling position for symbol:{}", symbol);
                    api.requestNewMarketOrder(symbol, quantity, OrderSide.SELL, OrderTimeInForce.DAY);
                } else {
                    getContext().getLog().info("No position to sell for symbol:{}, how did I reach this point in the" +
                            " strategy?", symbol);
                }
            }
        } catch (
                AlpacaAPIRequestException e) {
            getContext().getLog().error("Error when placing {} order for symbol:{}", orderSide.getAPIName(), symbol);
            e.printStackTrace();
        }
    }

    private boolean buyOrderAlreadyExists(String symbol) throws AlpacaAPIRequestException {
        return api.getOrders(OrderStatus.OPEN, MAX_NUMBER_OF_ORDERS, MIN_DATE, ZonedDateTime.now(),
                SortDirection.ASCENDING, false, Collections.singletonList(symbol)).size() != 0;
    }
}
