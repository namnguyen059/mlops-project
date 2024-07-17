```yaml
version: '3.7'
services:
  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop2.7.4-java8
    container_name: namenode
    environment:
      - CLUSTER_NAME=test
      - CORE_CONF_fs_defaultFS=hdfs://namenode:8020
    ports:
      - "50070:50070"
      - "9000:9000"
    volumes:
      - namenode-data:/hadoop/dfs/name
    networks:
      - hadoop

  datanode:
    image: bde2020/hadoop-datanode:2.0.0-hadoop2.7.4-java8
    container_name: datanode
    environment:
      - CORE_CONF_fs_defaultFS=hdfs://namenode:8020
      - CORE_CONF_hdfs_datanode_address=0.0.0.0:50010
      - CORE_CONF_hdfs_datanode_http_address=0.0.0.0:50075
      - CORE_CONF_hdfs_datanode_ipc_address=0.0.0.0:8010
      - CORE_CONF_hdfs_datanode_shutdown=0.0.0.0:8030
    volumes:
      - datanode-data:/hadoop/dfs/data
    links:
      - namenode
    networks:
      - hadoop

  hive-server:
    image: bde2020/hive:2.3.2-postgresql-metastore
    env_file:
      - ./hadoop-hive.env
    environment:
      HIVE_CORE_CONF_javax_jdo_option_ConnectionURL: "jdbc:postgresql://hive-metastore/metastore"
      SERVICE_PRECONDITION: "hive-metastore:9083"
    ports:
      - "10000:10000"
    networks:
      - hadoop

  hive-metastore:
    image: bde2020/hive:2.3.2-postgresql-metastore
    env_file:
      - ./hadoop-hive.env
    command: /opt/hive/bin/hive --service metastore
    environment:
      SERVICE_PRECONDITION: "namenode:50070 datanode:50075 hive-metastore-postgresql:5432"
    ports:
      - "9083:9083"
    networks:
      - hadoop

  hive-metastore-postgresql:
    image: bde2020/hive-metastore-postgresql:2.3.0
    networks:
      - hadoop

  spark:
    image: bitnami/spark:3.2.1
    container_name: spark
    ports:
      - "4040:4040"
      - "8080:8080"
    networks:
      - hadoop
    environment:
      - SPARK_MODE=master

  spark-worker:
    image: bitnami/spark:3.2.1
    container_name: spark-worker
    networks:
      - hadoop
    environment:
      - SPARK_MODE=worker
      - SPARK_MASTER_URL=spark://spark:7077

networks:
  hadoop:

volumes:
  namenode-data:
  datanode-data:
```

```bash
docker-compose down
```

```bash
docker-compose up -d
```

```bash
docker cp /Users/nguyennam/Desktop/Mlops/house_prices.csv namenode:/house_prices.csv
```

```bash
docker exec -it namenode /bin/bash
hdfs dfs -mkdir /data
hdfs dfs -put /house_prices.csv /data/house_prices.csv
exit
```


```bash
docker exec -it namenode /bin/bash
```


```bash
hdfs dfs -mkdir -p /user/hive/warehouse
```


```bash
hdfs dfs -chmod -R 775 /user/hive/warehouse
hdfs dfs -chown -R spark:supergroup /user/hive/warehouse
```

```bash
exit  
```


```py
from pyspark.sql import SparkSession

# Initialize Spark Session
spark = SparkSession.builder \
    .appName("Preprocess House Prices") \
    .config("hive.metastore.uris", "thrift://hive:9083") \
    .enableHiveSupport() \
    .getOrCreate()

# Load Data from HDFS
df = spark.read.csv("hdfs://namenode:8020/data/house_prices.csv", header=True, inferSchema=True)

# Perform Preprocessing (Example: selecting features and target variable)
df = df.select("feature1", "feature2", "price")

# Save Data to Hive
df.write.mode("overwrite").saveAsTable("default.house_prices_features")

spark.stop()
```

```bash
docker cp preprocess_data.py spark:/preprocess_data.py
```

```bash
docker exec -it spark /bin/bash
spark-submit --master spark://spark:7077 /preprocess_data.py
exit
```

```bash
docker exec -it mlops-hive-server-1 /bin/bash
hive
SHOW TABLES;
SELECT * FROM default.house_prices_features LIMIT 10;
```
