package ch.xavier.backtester.strategy;

import ch.xavier.backtester.strategy.analyzer.Analyzable;
import ch.xavier.backtester.strategy.analyzer.AnalyzableStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static reactor.core.publisher.Flux.*;


@Slf4j
public enum StrategiesFactory {

    INSTANCE;

    public Flux<AnalyzableStrategy> generateAllVariations(Strategies strategy, Flux<Object[]> combinationsOfParametersToExclude) {
        List<Supplier<Flux<Object>>> parametersSuppliers = new ArrayList<>();
        Class<? extends AnalyzableStrategy> className = strategy.className();

        fillParametersSuppliers(parametersSuppliers, className);

        Constructor strategyConstructor = className.getConstructors()[0];

        List<Object[]> parametersToExclude = combinationsOfParametersToExclude.collectList().block();

        return createStrategies(parametersSuppliers, strategyConstructor, parametersToExclude);
    }

    private void fillParametersSuppliers(List<Supplier<Flux<Object>>> parametersSuppliers, Class<? extends AnalyzableStrategy> className) {
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Analyzable.class)) {
                parametersSuppliers.add(() -> getAllPossibleValues(field));
            }
        }
    }


    private Flux<AnalyzableStrategy> createStrategies(List<Supplier<Flux<Object>>> parametersSuppliers, Constructor strategyConstructor,
                                                      List<Object[]> combinationsOfParametersToExclude) {
        //TODO: Make this generic somehow
        return switch (parametersSuppliers.size()) {
            case 1 -> parametersSuppliers.get(0).get().flatMap(param1 -> createStrategyFromParameters(strategyConstructor,
                    combinationsOfParametersToExclude, param1));
            case 2 -> parametersSuppliers.get(0).get().flatMap(param1 ->
                    parametersSuppliers.get(1).get().flatMap(param2 ->
                            createStrategyFromParameters(strategyConstructor, combinationsOfParametersToExclude, param1,
                                    param2)));
            case 3 -> parametersSuppliers.get(0).get().flatMap(param1 ->
                    parametersSuppliers.get(1).get().flatMap(param2 ->
                            parametersSuppliers.get(2).get().flatMap(param3 ->
                                    createStrategyFromParameters(strategyConstructor, combinationsOfParametersToExclude,
                                            param1, param2, param3))));
            case 4 -> parametersSuppliers.get(0).get().flatMap(param1 ->
                    parametersSuppliers.get(1).get().flatMap(param2 ->
                            parametersSuppliers.get(2).get().flatMap(param3 ->
                                    parametersSuppliers.get(3).get().flatMap(param4 ->
                                            createStrategyFromParameters(strategyConstructor, combinationsOfParametersToExclude,
                                                    param1, param2, param3, param4)))));
            case 5 -> parametersSuppliers.get(0).get().flatMap(param1 ->
                    parametersSuppliers.get(1).get().flatMap(param2 ->
                            parametersSuppliers.get(2).get().flatMap(param3 ->
                                    parametersSuppliers.get(3).get().flatMap(param4 ->
                                            parametersSuppliers.get(4).get().flatMap(param5 ->
                                                    createStrategyFromParameters(strategyConstructor, combinationsOfParametersToExclude,
                                                            param1, param2, param3, param4, param5))))));
            default -> throw new IllegalArgumentException("StrategiesFactory doesn't handle {} parameters yet, " +
                    "please add a case in the switch or make it generic." + parametersSuppliers.size());
        };
    }


    private Mono<AnalyzableStrategy> createStrategyFromParameters(Constructor<AnalyzableStrategy> strategyConstructor,
                                                                  List<Object[]> combinationsOfParametersToExclude, Object... params) {

        for (Object[] paramsToExclude : combinationsOfParametersToExclude) { //takes a few seconds for half a million strats, to be optimized when needed
            if (Arrays.equals(paramsToExclude, params)) {
                log.debug("The strategy with parameters:{} is already present in DB", params);

                combinationsOfParametersToExclude.remove(paramsToExclude);
                return Mono.empty();
            }
        }

        try {
            return Mono.just(strategyConstructor.newInstance(params));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Problem when creating instance of strategy:{} with params:{}.\n",
                    strategyConstructor.getDeclaringClass(), params, e);
            return Mono.empty();
        }
    }


    private Flux<Object> getAllPossibleValues(Field field) {
        if (field.getType().isEnum()) {
            Class enumClass = field.getType();

            try {
                Field enumField = enumClass.getDeclaredField("$VALUES");
                enumField.setAccessible(true);

                Object[] enumValues = (Object[]) enumField.get(null);
                return Flux.fromArray(enumValues);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("There was an error when iterating the enum {}, please check why:", field.getType().getName(), e);
            }
        } else if (field.getType().equals(String.class)) {
            return Flux.fromArray(field.getAnnotation(Analyzable.class).stringValues());
        }

        int minValue = field.getAnnotation(Analyzable.class).minValue();
        int maxValue = field.getAnnotation(Analyzable.class).maxValue() + 1;
        int[] additionalValues = field.getAnnotation(Analyzable.class).additionalValues();

        Flux<Integer> rangeFlux = empty();
        Flux<Object> additionalValuesFlux = fromStream(IntStream.of(additionalValues).boxed());

        if (minValue < maxValue) {
            rangeFlux = range(minValue, maxValue + -minValue);
        }

        return concat(rangeFlux, additionalValuesFlux)
                .filter(value -> !value.equals(0));
    }
}