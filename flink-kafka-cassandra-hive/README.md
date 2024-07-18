****real-time data ingestion using Kafka and store it in  Cassandra and Hive Feature Stores with Apache Flink****


```bash
wget https://archive.apache.org/dist/flink/flink-1.18.1/flink-1.18.1-bin-scala_2.11.tgz
tar -xvzf flink-1.18.1-bin-scala_2.11.tgz
cd flink-1.18.1
```


```bash
./bin/start-cluster.sh
```


```bash
mvn clean package
```

```bash
/Users/nguyennam/flink-1.18.1/bin/flink run -c com.example.FlinkKafkaToCassandraAndHive target/FlinkKafkaToCassandraAndHive-1.0-SNAPSHOT.jar
```

