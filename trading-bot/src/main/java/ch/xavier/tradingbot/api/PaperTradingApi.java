package ch.xavier.tradingbot.api;

import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaperTradingApi {

    private final AlpacaAPI api;

    public PaperTradingApi() {
        this.api = new AlpacaAPI();
        //always throw AlpacaAPIRequestException if a problem occurs

        //DOCS
        //https://github.com/Petersoj/alpaca-java
        //https://ta4j.github.io/ta4j-wiki/Live-trading.html
    }
}
