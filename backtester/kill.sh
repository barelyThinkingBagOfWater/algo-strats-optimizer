ps -aef | grep backtester- | awk '{print $2}' | xargs kill -9
