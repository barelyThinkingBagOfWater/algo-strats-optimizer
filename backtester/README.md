This artifact backtests algorithmic trading strategies with different parameters on past quotes and saves the results in db for further analysis.

I want to refactor it so that adding a new CustomStrategy.java (with annotations) will be enough. I also would like a cli interface for easier use (now please use the test classes or the constructor of StrategiesAnalyzer to run the analysis). 

The backtesting is done on all available cores and will consume a lot of resources while running.


Specific strategies results are needed because of the indexing of each parameter for each strategy in Mongo. Could be dynamic though? Maybe not necessary when actually using the results, refactor this artifact later.