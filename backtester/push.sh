mvn clean install && 
	docker build . -t localhost:5000/backtester &&
	docker push localhost:5000/backtester