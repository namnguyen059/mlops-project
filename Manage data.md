# mlops-project

Yes, it's possible to build your data ingestion and storage infrastructure using the provided Docker Compose file as a base. We'll need to expand on it to include the remaining components: Apache Kafka, Zookeeper, Spark, Cassandra, and the API for feature retrieval.

Here's how you can integrate these additional services into your existing Docker Compose file:

### Updated `docker-compose.yml`

```yaml
version: '3.8'

services:
  zookeeper:
    image: wurstmeister/zookeeper:3.4.6
    container_name: zookeeper
    ports:
      - "2181:2181"
    platform: linux/amd64

  kafka:
    image: wurstmeister/kafka:2.12-2.2.1
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093,OUTSIDE://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE 
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      - zookeeper
    platform: linux/amd64
 
  cassandra:
    image: cassandra:3.11
    container_name: cassandra
    ports:
      - "9042:9042"
    networks:
      default:
        aliases:
          - cassandra
    platform: linux/amd64

  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop2.7.4-java8
    container_name: namenode
    ports:
      - "9870:9870"
      - "9000:9000"
    environment:
      - CLUSTER_NAME=test
    volumes:
      - namenode:/hadoop/dfs/name
    platform: linux/amd64

  datanode:
    image: bde2020/hadoop-datanode:2.0.0-hadoop2.7.4-java8
    container_name: datanode
    ports:
      - "9864:9864"
    environment:
      - CLUSTER_NAME=test
      - CORE_CONF_fs_defaultFS=hdfs://namenode:9000
    volumes:
      - datanode:/hadoop/dfs/data
    platform: linux/amd64

  hive-metastore-postgresql:
    image: postgres:12
    container_name: hive-metastore-postgresql
    environment:
      - POSTGRES_DB=metastore
      - POSTGRES_USER=hive
      - POSTGRES_PASSWORD=hive
    ports:
      - "5432:5432"
    platform: linux/amd64

  hive-metastore:
    image: bde2020/hive:2.3.2-postgresql-metastore
    container_name: hive-metastore
    environment:
      - HIVE_METASTORE_DB_TYPE=postgres
      - HIVE_METASTORE_DB_HOST=hive-metastore-postgresql
      - HIVE_METASTORE_DB_PORT=5432
      - HIVE_METASTORE_DB_NAME=metastore
      - HIVE_METASTORE_DB_USER=hive
      - HIVE_METASTORE_DB_PASS=hive
      - HIVE_METASTORE_HOST=hive-metastore
    ports:
      - "9083:9083"
    depends_on:
      - hive-metastore-postgresql
    platform: linux/amd64

  hive-server:
    image: bde2020/hive:2.3.2
    container_name: hive-server
    environment:
      - HIVE_METASTORE_URI=thrift://hive-metastore:9083
    ports:
      - "10000:10000"
    depends_on:
      - hive-metastore
    platform: linux/amd64

  presto-coordinator:
    image: shawnzhu/prestodb:0.181
    container_name: presto-coordinator
    ports:
      - "8081:8080" # Changed the host port to 8081
    environment:
      - PRESTO_JVM_HEAP_SIZE=2G
      - DISCOVERY_SERVER_ENABLED=true
      - NODE_ENVIRONMENT=test
    platform: linux/amd64

  spark-master:
    image: bde2020/spark-master:2.4.4-hadoop2.7
    container_name: spark-master
    ports:
      - "8082:8080" # Changed the host port to 8082
      - "7077:7077"
    environment:
      - INIT_DAEMON_STEP=setup_spark
      - SPARK_MODE=master
    platform: linux/amd64

  spark-worker:
    image: bde2020/spark-worker:2.4.4-hadoop2.7
    container_name: spark-worker
    ports:
      - "8083:8081" # Changed the host port to 8083
    environment:
      - SPARK_MODE=worker
      - SPARK_MASTER=spark://spark-master:7077
    depends_on:
      - spark-master
    platform: linux/amd64

  api:
    build: ./api
    container_name: api
    ports:
      - "5001:5000"
    depends_on:
      - cassandra
    platform: linux/amd64

volumes:
  namenode:
  datanode:
```

### 2. Adding the API Service

Create a directory `api` with a `Dockerfile` and an API implementation file `app.py`.

**api/Dockerfile:**

```Dockerfile
FROM python:3.8-slim

WORKDIR /app

COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt

COPY . .

CMD ["python", "app.py"]
```

**api/requirements.txt:**

```txt
flask
cassandra-driver
```

**api/app.py:**

```python
from flask import Flask, request, jsonify
from cassandra.cluster import Cluster

app = Flask(__name__)
cluster = Cluster(['cassandra'])
session = cluster.connect('feature_store')

@app.route('/features/<id>', methods=['GET'])
def get_feature(id):
    query = "SELECT * FROM features WHERE id=%s"
    rows = session.execute(query, [id])
    feature = rows.one()
    if feature:
        return jsonify({
            'id': feature.id,
            'feature_data': feature.feature_data,
            'timestamp': feature.timestamp
        })
    else:
        return jsonify({'error': 'Feature not found'}), 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

### 3. Running the Setup

1. **Build and Run Containers**:
   ```sh
   docker-compose up --build
   ```

2. **Initialize Hive Schema**:
   ```sh
   docker-compose exec hive-server bash
   schematool -dbType postgres -initSchema
   ```

3. **Create Keyspaces and Tables in Cassandra**:
   ```sh
   docker-compose exec cassandra cqlsh
   ```

   ```sql
   CREATE KEYSPACE feature_store WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
   USE feature_store;
   CREATE TABLE features (
     id UUID PRIMARY KEY,
     feature_data TEXT,
     timestamp TIMESTAMP
   );
   ```

### 4. Ingesting Data into HDFS

You can use the `hdfs dfs -put` command to ingest data into HDFS.

```sh
docker-compose exec namenode bash
hdfs dfs -mkdir /data/raw
hdfs dfs -put local-file.txt /data/raw/
```

### 5. Running Spark Jobs

Submit Spark jobs using the Spark master container:

```sh
docker-compose exec spark-master bash
spark-submit --class com.example.DataPreprocessing --master spark://spark-master:7077 path/to/jarfile.jar
```

### Summary

This Docker Compose setup integrates Kafka, Zookeeper, Spark, Cassandra, HDFS, Hive, and the API service for feature retrieval. This configuration provides a scalable, reproducible environment for managing data pipelines and machine learning workflows. By running `docker-compose up --build`, you can spin up the entire infrastructure and start using it for data ingestion, storage, and processing.
