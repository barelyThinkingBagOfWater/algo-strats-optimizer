#!/usr/bin/env bash
mvn clean install -DskipTests && docker build . -t backtester && docker run --memory=10g --memory-swap=-1 --rm --network isolatedNetwork --name backtester backtester
