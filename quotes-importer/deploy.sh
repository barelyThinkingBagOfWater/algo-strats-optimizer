#!/usr/bin/env bash
mvn clean install && docker build . -t quotes-importer && docker run --rm --network isolatedNetwork --name quotes-importer quotes-importer
