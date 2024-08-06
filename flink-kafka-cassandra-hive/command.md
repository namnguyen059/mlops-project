```bash
docker exec -it broker kafka-topics --create --topic house_prices_live --bootstrap-server broker:9092 --partitions 1 --replication-factor 1
```

```bash
mvn archetype:generate -DgroupId=com.example -DartifactId=flink-kafka-cassandra-hive -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

```bash
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cassandra
```

```bash
docker exec -it cassandra cqlsh
```

```sql
CREATE KEYSPACE IF NOT EXISTS house_prices WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE house_prices;

CREATE TABLE house_prices.feature_store (
    feature1 DOUBLE,
    feature2 DOUBLE,
    price DOUBLE,
    PRIMARY KEY (feature1, feature2)
);

SELECT * FROM feature_store;
```

```bash
mvn clean package
```

```bash
docker cp /Users/nguyennam/Desktop/Mlops/flink-kafka-cassandra-hive/target/FlinkKafkaToCassandraAndHive-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/ 
```

```bash
docker exec -it  flink-jobmanager bash
```

```bash
/opt/flink/bin/flink run -c com.example.FlinkKafkaToCassandraAndHive FlinkKafkaToCassandraAndHive-1.0-SNAPSHOT.jar
```


