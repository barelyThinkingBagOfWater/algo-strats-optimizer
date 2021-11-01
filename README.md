# algo-strats-optimizer

An ongoing project about a system that automatically optimizes algorithmic financial strategies and test them using a paper trading API composed by:

- quotes-importer : Imports quotes and historical data from providers on which to backtest the strategies
- ta4j : Enriched open-source Java library for technical analysis (mainly used for its technical indicators)
- backtester : Backtests the algorithmic financial strategies on the quotes by basically bruteforcing different parameters (using dedicated Java annotations)
- tradingbot: A tradingbot using Akka that tests the resulting strategies in real conditions through a Paper trading api (Alpaca)
