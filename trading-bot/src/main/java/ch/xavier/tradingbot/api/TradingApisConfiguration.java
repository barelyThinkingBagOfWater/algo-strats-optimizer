package ch.xavier.tradingbot.api;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.DomainType;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.enums.api.DataAPIType;
import net.jacobpeterson.alpaca.enums.api.EndpointAPIType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingApisConfiguration {

    @Bean
    public AlpacaAPI alpacaApi() { //Cannot read alpaca.properties anymore? Client restricted to paper trading api
        return new AlpacaAPI(
                "PK6GUN1VQJBJDSANE6Z5",
                "0NN9gunuCZSV0HXcZJpgTadK6Psekd3IMD1tAs0N",
                EndpointAPIType.PAPER,
                DataAPIType.IEX);
    }

    @Bean
    public BinanceApiClientFactory binanceApi() { //Hello Github! It's a read-only api client anyway with ip restrictions
        return BinanceApiClientFactory.newInstance(
                "8atolWX83xZvAutu4m7wz77GSds9WewRdpiyfLQlmt4RYpYK2F1WZwQHTapaZs8U",
                "kTG3rJPxKPNeIlGis357D9UzIbk0Mj6Nqns45NixQJ3VDiylS7cVJr9U9aEdJAGn", DomainType.Com);
    }
}
