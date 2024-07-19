package com.example;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.catalog.hive.HiveCatalog;

import java.util.Properties;

@SuppressWarnings("deprecation")
public class FlinkKafkaToCassandraAndHive {
    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // // Set up Hive Catalog
        // String name = "myhive";
        // String defaultDatabase = "default";
        // String hiveConfDir = "/opt/hive/conf"; 
        // HiveCatalog hiveCatalog = new HiveCatalog(name, defaultDatabase, hiveConfDir);
        // tableEnv.registerCatalog("myhive", hiveCatalog);
        // tableEnv.useCatalog("myhive");

        // Set up Kafka consumer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "house_prices");
        properties.setProperty("request.timeout.ms", "60000");
        properties.setProperty("retry.backoff.ms", "2000");
        properties.setProperty("metadata.max.age.ms", "60000");

        // Create a Kafka consumer
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
                "house_prices_live",
                new SimpleStringSchema(),
                properties
        );

        // Add the consumer to the data stream
        DataStream<String> stream = env.addSource(consumer);

        // Parse the incoming data
        DataStream<Tuple3<Float, Float, Float>> parsedStream = stream.map(new MapFunction<String, Tuple3<Float, Float, Float>>() {
            @Override
            public Tuple3<Float, Float, Float> map(String value) throws Exception {
                String[] fields = value.split(",");
                return new Tuple3<>(
                        Float.parseFloat(fields[0]),
                        Float.parseFloat(fields[1]),
                        Float.parseFloat(fields[2])
                );
            }
        });

        // Sink to Cassandra
        CassandraSink.addSink(parsedStream)
                .setHost("172.18.0.10")  // Adjust 
                .setQuery("INSERT INTO house_prices.feature_store (feature1, feature2, price) VALUES (?, ?, ?);")
                .build();

        // // Register the data stream as a table in Flink
        // tableEnv.createTemporaryView("house_prices_table", parsedStream,
        //         Schema.newBuilder()
        //                 .column("f0", "FLOAT")
        //                 .column("f1", "FLOAT")
        //                 .column("f2", "FLOAT")
        //                 .build()
        // );

        // // Write to Hive
        // tableEnv.executeSql(
        //         "CREATE TABLE IF NOT EXISTS house_prices_live (feature1 FLOAT, feature2 FLOAT, price FLOAT) " +
        //         "WITH ('connector' = 'hive', 'hive.metastore.uris' = 'thrift://hive-metastore:9083')"
        // );
        // tableEnv.executeSql(
        //         "INSERT INTO house_prices_live SELECT f0 AS feature1, f1 AS feature2, f2 AS price FROM house_prices_table"
        // );

        // Execute the Flink job
        env.execute("Flink Kafka to Cassandra and Hive");
    }
}
