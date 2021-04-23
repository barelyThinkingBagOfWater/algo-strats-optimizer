package ch.xavier.tradingbot;

import akka.actor.typed.ActorSystem;
import ch.xavier.tradingbot.quote.MongoQuotesRepository;
import ch.xavier.tradingbot.strategies.StrategiesInitializer;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TradingBot {

    @Autowired
    public TradingBot(MongoQuotesRepository repository, AlpacaAPI api) {

        ActorSystem.create(StrategiesInitializer.create(repository, api), "strategiesInitializer");
    }
}
