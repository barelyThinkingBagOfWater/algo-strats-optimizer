FROM openjdk:14

ADD target/backtester-0.0.1-SNAPSHOT.jar /backtester.jar
ENTRYPOINT ["java","-jar","-verbose:gc,","/backtester.jar"]
