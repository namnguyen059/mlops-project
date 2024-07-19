package com.example;

import com.example.Deserializer.JSONValueDeserializationSchema;
import com.example.Dto.Features;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
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
        properties.setProperty("bootstrap.servers", "broker:29092");
        properties.setProperty("group.id", "house_prices");
        properties.setProperty("request.timeout.ms", "60000");
        properties.setProperty("retry.backoff.ms", "2000");
        properties.setProperty("metadata.max.age.ms", "60000");

        // Create a Kafka source
        String topic = "house_prices_live";
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("broker:29092")
                .setTopics(topic)
                .setGroupId("flink-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Add the source to the data stream
        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Parse and filter the incoming data
        DataStream<Tuple3<Double, Double, Double>> parsedStream = stream
            .filter(value -> {
                String[] fields = value.split(",");
                return fields.length == 3 && fields[0].matches("-?\\d+(\\.\\d+)?") 
                    && fields[1].matches("-?\\d+(\\.\\d+)?") 
                    && fields[2].matches("-?\\d+(\\.\\d+)?");
            })
            .map(value -> {
                String[] fields = value.split(",");
                return new Tuple3<>(
                        Double.parseDouble(fields[0]),
                        Double.parseDouble(fields[1]),
                        Double.parseDouble(fields[2])
                );
            }).returns(TypeInformation.of(new TypeHint<Tuple3<Double, Double, Double>>() {})); 


        // Sink to Cassandra
        CassandraSink.addSink(parsedStream)
                .setHost("172.18.0.10")  // Adjust to your Cassandra host
                .setQuery("INSERT INTO house_prices.feature_store (feature1, feature2, price) VALUES (?, ?, ?);")
                .build();

        // // Register the data stream as a table in Flink
        // tableEnv.createTemporaryView("house_prices_table", parsedStream,
        //         Schema.newBuilder()
        //                 .column("f0", "Double")
        //                 .column("f1", "Double")
        //                 .column("f2", "Double")
        //                 .build()
        // );

        // // Write to Hive
        // tableEnv.executeSql(
        //         "CREATE TABLE IF NOT EXISTS house_prices_live (feature1 Double, feature2 Double, price Double) " +
        //         "WITH ('connector' = 'hive', 'hive.metastore.uris' = 'thrift://hive-metastore:9083')"
        // );
        // tableEnv.executeSql(
        //         "INSERT INTO house_prices_live SELECT f0 AS feature1, f1 AS feature2, f2 AS price FROM house_prices_table"
        // );

        // Execute the Flink job
        env.execute("Flink Kafka to Cassandra and Hive");
    }
}
