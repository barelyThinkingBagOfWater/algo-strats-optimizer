package ch.xavier.tradingbot.api;

import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.AlpacaAPI;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class paperTradingApi {

    private final AlpacaAPI api;

    public paperTradingApi() {
        this.api = new AlpacaAPI();
        //always throw AlpacaAPIRequestException if a problem occurs
    }
}
