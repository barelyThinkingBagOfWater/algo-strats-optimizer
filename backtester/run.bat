#mvn clean install -DskipTests && java -jar -Xmx16g -Xms16g target/backtester-0.0.1-SNAPSHOT.jar
mvn clean install -DskipTests && java -jar -Xmx8g -Xms8g target/backtester-0.0.1-SNAPSHOT.jar

#Additional usable args
# -verbose:gc
