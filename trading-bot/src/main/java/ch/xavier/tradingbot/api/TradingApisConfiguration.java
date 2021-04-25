package ch.xavier.tradingbot.api;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.enums.api.DataAPIType;
import net.jacobpeterson.alpaca.enums.api.EndpointAPIType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingApisConfiguration {

    @Bean
    public AlpacaAPI api() { //cannot read alpaca.properties anymore? Akka must interfere with something.
        return new AlpacaAPI(
                "PK9SHVCP9L3OVBASJOIW",
                "KO0kfGze9SzpeP6XMnqru58ZpH92e3PijkYUV5F3",
                EndpointAPIType.PAPER,
                DataAPIType.IEX);
    }

        //DOCS
        //https://github.com/Petersoj/alpaca-java
        //https://ta4j.github.io/ta4j-wiki/Live-trading.html
}
