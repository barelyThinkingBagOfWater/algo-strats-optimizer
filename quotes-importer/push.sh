mvn clean install && 
	docker build . -t localhost:5000/quotes-importer &&
	docker push localhost:5000/quotes-importer