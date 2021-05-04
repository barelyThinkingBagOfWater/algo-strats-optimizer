package ch.xavier.tradingbot;

import akka.actor.typed.ActorSystem;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TradingBot {

    @Autowired
    public TradingBot(MongoQuotesRepository quotesRepository, AlpacaAPI tradingApi) {
        ActorSystem.create(ActorsInitializer.create(quotesRepository, tradingApi), "ActorsInitializer");
    }
}
