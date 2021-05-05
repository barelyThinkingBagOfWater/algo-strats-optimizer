package ch.xavier.backtester.strategy.analyzer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Analyzable {
    int minValue() default 0;
    int maxValue() default 0;
    int[] additionalValues() default {};
    String[] stringValues() default {};
}
