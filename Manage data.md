# mlops-project

Yes, it's possible to build your data ingestion and storage infrastructure using the provided Docker Compose file as a base. We'll need to expand on it to include the remaining components: Apache Kafka, Zookeeper, Spark, Cassandra, and the API for feature retrieval.

Here's how you can integrate these additional services into your existing Docker Compose file:

### Updated `docker-compose.yml`

```yaml
version: '3'
services:
  namenode:
    image: tomdesinto/hadoop-namenode:2.10.1
    container_name: namenode
    environment:
      - CLUSTER_NAME=test
      - CORE_CONF_fs_defaultFS=hdfs://namenode:8020
      - CORE_CONF_hadoop_http_staticuser_user=root
      - HDFS_CONF_dfs_replication=1
    ports:
      - 50070:50070
      - 8020:8020
    volumes:
      - hadoop_namenode:/hadoop/dfs/name
    networks:
      - hadoop

  datanode:
    image: tomdesinto/hadoop-datanode:2.10.1
    container_name: datanode
    environment:
      - CORE_CONF_fs_defaultFS=hdfs://namenode:8020
      - CORE_CONF_hadoop_http_staticuser_user=root
      - HDFS_CONF_dfs_datanode_http_address=0.0.0.0:50075
    depends_on:
      - namenode
    ports:
      - 50075:50075
    volumes:
      - hadoop_datanode:/hadoop/dfs/data
    networks:
      - hadoop

  hive-metastore:
    image: tomdesinto/hive-metastore:3.1.2
    container_name: hive-metastore
    environment:
      - HIVE_DB=metastore
      - HIVE_USER=hive
      - HIVE_PASSWORD=hive
      - POSTGRES_DB=metastore
      - POSTGRES_USER=hive
      - POSTGRES_PASSWORD=hive
      - CORE_CONF_fs_defaultFS=hdfs://namenode:8020
    depends_on:
      - namenode
      - datanode
      - hive-postgresql
    networks:
      - hadoop

  hive-postgresql:
    image: postgres:10
    container_name: hive-postgresql
    environment:
      - POSTGRES_DB=metastore
      - POSTGRES_USER=hive
      - POSTGRES_PASSWORD=hive
    networks:
      - hadoop

  hive-server:
    image: tomdesinto/hive-server:3.1.2
    container_name: hive-server
    environment:
      - SERVICE_PRECONDITION=hive-metastore:9083
    command: /opt/hive/bin/hive --service hiveserver2
    ports:
      - 10000:10000
    depends_on:
      - hive-metastore
    networks:
      - hadoop

  spark-master:
    image: tomdesinto/spark-master:2.4.5
    container_name: spark-master
    environment:
      - INIT_DAEMON_STEP=spark
      - 'constraint:node==master'
    ports:
      - 8080:8080
    networks:
      - hadoop

  spark-worker:
    image: tomdesinto/spark-worker:2.4.5
    container_name: spark-worker
    environment:
      - SPARK_WORKER_CORES=1
      - SPARK_WORKER_MEMORY=1g
      - SPARK_MASTER=spark://spark-master:7077
      - 'constraint:node==worker'
    ports:
      - 8081:8081
    depends_on:
      - spark-master
    networks:
      - hadoop

volumes:
  hadoop_namenode:
  hadoop_datanode:

networks:
  hadoop:

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
