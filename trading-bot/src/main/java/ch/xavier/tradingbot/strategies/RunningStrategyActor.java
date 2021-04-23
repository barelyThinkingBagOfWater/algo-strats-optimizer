package ch.xavier.tradingbot.strategies;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import ch.xavier.tradingbot.realtime.NewBarMessage;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public class RunningStrategyActor extends AbstractBehavior<NewBarMessage> {

    private final Strategy strategy;
    private final BarSeries series;

    public static Behavior<NewBarMessage> create(RunnableStrategy strategy, BarSeries series) {
        return Behaviors.setup(context -> new RunningStrategyActor(context, strategy, series));
    }

    private RunningStrategyActor(ActorContext<NewBarMessage> context, RunnableStrategy runnableStrategy, BarSeries series) {
        super(context);
        this.series = series;
        this.strategy = runnableStrategy.buildStrategy(series);

        getContext().getLog().info("Strategy:{} with parameters:{} is running in dedicated actor for symbol:{}",
                strategy.getName(), strategy.getParameters(), series.getName());
    }


    @Override
    public Receive<NewBarMessage> createReceive() {
        return newReceiveBuilder().onMessage(NewBarMessage.class, this::onReceive).build();
    }

    private Behavior<NewBarMessage> onReceive(NewBarMessage message) {
        getContext().getLog().info("Received message:{}", message);

        series.addBar(message.bar());
        int endIndex = series.getEndIndex();

        if (strategy.shouldEnter(endIndex)) {
            getContext().getLog().info("strategy should enter, buying stock");
        } else if (strategy.shouldExit(endIndex)) {
            getContext().getLog().info("strategy should exit, selling stock");
        }

        return this;
    }
}
