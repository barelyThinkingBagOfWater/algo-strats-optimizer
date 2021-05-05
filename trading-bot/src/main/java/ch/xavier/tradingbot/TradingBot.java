package ch.xavier.tradingbot;

import akka.actor.typed.ActorSystem;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import com.binance.api.client.BinanceApiClientFactory;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TradingBot {

    @Autowired
    public TradingBot(MongoQuotesRepository quotesRepository, AlpacaAPI tradingApi,
                      BinanceApiClientFactory binanceApiClientFactory) {
        ActorSystem.create(ActorsInitializer.create(quotesRepository, tradingApi, binanceApiClientFactory), "ActorsInitializer");
    }
}
