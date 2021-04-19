package ch.xavier.backtester.result;

import ch.xavier.backtester.quote.typed.QuoteType;
import ch.xavier.backtester.strategy.Strategies;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Set;

@Repository
@Slf4j
public class MongoResultsRepository {

    private static final String DATABASE_NAME = "results";


    private final ReactiveMongoTemplate template;
    private final MongoClient webclient;

    @Autowired
    public MongoResultsRepository(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        webclient = MongoClients.create(mongoUri.replace("%database", DATABASE_NAME));
        this.template = new ReactiveMongoTemplate(webclient, DATABASE_NAME);

        this.template.setWriteConcern(WriteConcern.ACKNOWLEDGED); //server acks write operations
        this.template.setWriteResultChecking(WriteResultChecking.EXCEPTION);  //exception if write fails
    }


    public Mono<StrategyResult> save(StrategyResult result, String collectionName) {
        return template.save(result, collectionName);
    }

    public Mono<Void> dropCollection(String collectionName) {
        return template.dropCollection(collectionName);
    }

    public Mono<Long> countResultsInCollection(final String collectionName) {
        return template.count(new Query(), collectionName);
    }

    public Mono<Long> countSpecificResultsForSymbolInCollection(final String symbol, final String collectionName) {
        return template.count(new Query(Criteria.where("symbol").is(symbol)), collectionName);
    }

    public Mono<String> createCollectionAndIndexIfNeeded(final String collectionName, final Set<String> strategySpecificParameters) {
        if (!template.collectionExists(collectionName).block()) {
            log.debug("Creating collection:{}", collectionName);

            Index specificStrategyIndex = new Index().named("specificStrategyIndex").on("symbol", Sort.DEFAULT_DIRECTION);
            Flux.fromStream(strategySpecificParameters.stream())
                    .doOnNext(parameter -> specificStrategyIndex.on(parameter, Sort.DEFAULT_DIRECTION))
                    .blockLast();

            return template.createCollection(collectionName).then(
                    template.indexOps(collectionName).ensureIndex(specificStrategyIndex.unique()));
        } else {
            log.debug("Collection {} already exists, now resuming analysis.", collectionName);
            return Mono.empty();
        }
    }

    public Flux<Object[]> getParametersOfAlreadyAnalyzedStrategies(final String symbol, final Strategies strategy,
                                                                   final String collectionName) {

        return template.find(new Query(Criteria.where("symbol").is(symbol)), strategy.resultClassName(), collectionName)
                .map(StrategyResult::getParams);
    }


    //MORE RESULTS CONSULTATION, IN NEW ARTIFACT LATER
    public Flux<String> listAllCollectionsName() {
        return template.getCollectionNames();
    }


    public Flux<Double> getNBestValuesOfFieldInCollection(final int numberOfValues, final String fieldName,
                                                          final String collectionName) {
        return template
                .findDistinct(new Query(), fieldName, collectionName, Double.class)
                .filter(result -> !result.isNaN())
                .sort(Comparator.reverseOrder())
                .take(numberOfValues);
    }

    public Flux<StrategyResult> geResultsOfNBestValuesForFieldForStrategy(final int numberOfValues, final String fieldName,
                                                                          final Strategies strategy, final QuoteType quoteType) {
        final String collectionName = strategy.name() + " - " + quoteType.name();

        return getNBestValuesOfFieldInCollection(numberOfValues, fieldName, collectionName)
                .flatMap(value -> template.find(new Query(Criteria.where(fieldName).is(value)), strategy.resultClassName(),
                        collectionName));
    }
}