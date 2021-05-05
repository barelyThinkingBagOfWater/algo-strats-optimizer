package ch.xavier.quotes;

import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@Slf4j
public class MongoQuotesRepository {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoQuotesRepository(ReactiveMongoTemplate template) {
        this.template = template;

        createUniqueIndexesForAllQuotes().subscribe();
    }


    public Mono<Quote> save(final Quote quote) {
        return template.save(quote)
                .onErrorResume(e -> Mono.empty()); //So duplicates don't interrupt the flux,
        //filtering by ExceptionClass doesn't work (DuplicateKeyException)
    }

    public Flux<Quote> saveAll(final List<Quote> quotes) {
        return template.insertAll(quotes)
                .onErrorResume(e -> Mono.empty());
    }

    public Flux<? extends Quote> findAllBySymbol(final String symbol, final Class<? extends Quote> quoteType) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("symbol").is(symbol));
        query.with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return template.find(query, quoteType);
    }

    public Mono<DeleteResult> deleteAllQuotesOfType(final Class<? extends Quote> quoteType) {
        return template.remove(quoteType).all();
    }

    public Mono<DeleteResult> deleteAllQuotesOfTypeForSymbol(final Class<? extends Quote> quoteType, final String symbol) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("symbol").is(symbol));

        return template.remove(query, quoteType);
    }

    public Mono<Long> count(final Class<? extends Quote> quoteType) {
        return template.count(new Query(), quoteType);
    }

    private Flux createUniqueIndexesForAllQuotes() {
        return Flux.fromArray(Quote.QuoteType.values())
                .map(quoteType -> quoteType.getQuoteClass())
                .flatMap(quoteClass -> createUniqueIndexOnSymbolAndTimestamp(quoteClass));
    }

    public Mono<String> createUniqueIndexOnSymbolAndTimestamp(Class<? extends Quote> quoteClass) {
        return template.createCollection(quoteClass)
                .then(template.indexOps(quoteClass).ensureIndex(
                        new Index().named("uniqueIndex")
                                .on("symbol", Sort.Direction.ASC)
                                .on("timestamp", Sort.Direction.ASC).unique()))
                .onErrorResume(e -> Mono.empty()); //if the collection exist don't interrupt the Flux
    }

    public Flux<String> findAllSymbolsForQuoteType(final Class<? extends Quote> quoteType) {
        return template.findDistinct(new Query(), "symbol", quoteType, String.class);
    }
}