To achieve real-time data ingestion using Kafka and store it in both Cassandra and Hive Feature Stores with Apache Flink, follow the steps below:


```bash
wget https://archive.apache.org/dist/flink/flink-1.18.1/flink-1.18.1-bin-scala_2.11.tgz
tar -xvzf flink-1.18.1-bin-scala_2.11.tgz
cd flink-1.18.1
```


```bash
./bin/start-cluster.sh
```


```bash
mvn archetype:generate -DgroupId=com.example -DartifactId=flink-kafka-cassandra-hive -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cd flink-kafka-cassandra-hive
```


```bash
mvn clean package
```

```bash
/Users/nguyennam/flink-1.18.1/bin/flink run -c com.example.FlinkKafkaToCassandraAndHive target/FlinkKafkaToCassandraAndHive-1.0-SNAPSHOT.jar
```

