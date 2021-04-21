package ch.xavier.tradingbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TradingBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingBotApplication.class, args);
	}

	//1. Create strategies with chosen parameters, including symbol
	//2. Create listener and feed the bars to the relevant Runnable/what?

}
