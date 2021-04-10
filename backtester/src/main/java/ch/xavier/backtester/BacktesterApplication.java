package ch.xavier.backtester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication(exclude = MongoReactiveAutoConfiguration.class)
@EnableReactiveMongoRepositories
public class BacktesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BacktesterApplication.class, args);
    }

    //TODO: Interactive prompt avec Strat numérotée et tu entres num, idem avec quote type et symbol
}
