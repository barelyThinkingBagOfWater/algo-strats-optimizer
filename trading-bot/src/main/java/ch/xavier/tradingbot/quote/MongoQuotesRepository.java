package ch.xavier.tradingbot.quote;

import ch.xavier.tradingbot.quote.typed.DailyQuote;
import ch.xavier.tradingbot.quote.typed.QuoteType;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
@Slf4j
public class MongoQuotesRepository {

    private static final String DATABASE_NAME = "quotes";
    private final static int NY_ZONE_OFFSET = -4;

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoQuotesRepository(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        MongoClient webclient = MongoClients.create(mongoUri.replace("%database", DATABASE_NAME));
        this.template = new ReactiveMongoTemplate(webclient, DATABASE_NAME);
    }


    public Flux<? extends Quote> findAllBySymbol(final String symbol, QuoteType quoteType) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("symbol").is(symbol));
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return template.find(query, quoteType.getQuoteClass());
    }

    public Flux<? extends Quote> findAllBySymbolSinceDate(final String symbol, QuoteType quoteType, LocalDateTime localDateTime) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("symbol").is(symbol));
        query.addCriteria(Criteria.where("timestamp").gt(localDateTime.toEpochSecond(ZoneOffset.ofHours(NY_ZONE_OFFSET))));
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return template.find(query, quoteType.getQuoteClass());
    }

    public Mono<Long> count(final Class<? extends Quote> quoteType) {
        return template.count(new Query(), quoteType);
    }

    public Flux<String> getAllStoredSymbols(QuoteType quoteType) {
        return template.findDistinct("symbol", quoteType.getQuoteClass(), String.class);
    }
}