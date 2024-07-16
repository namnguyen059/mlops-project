### Updated `docker-compose.yml`

```yaml
version: '3'
services:
  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop2.7.4-java8
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
    image: bde2020/hadoop-datanode:2.0.0-hadoop2.7.4-java8
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
    image: bde2020/hive:2.3.2-postgresql-metastore
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
    image: bde2020/hive:2.3.2-postgresql-metastore
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
    image: bde2020/spark-master:2.4.0-hadoop2.7
    container_name: spark-master
    environment:
      - INIT_DAEMON_STEP=spark
      - 'constraint:node==master'
    ports:
      - 8080:8080
    networks:
      - hadoop

  spark-worker:
    image: bde2020/spark-worker:2.4.0-hadoop2.7
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

```yaml
version: '3'
services:
  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop2.7.4-java8
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
    image: bde2020/hadoop-datanode:2.0.0-hadoop2.7.4-java8
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
    image: bde2020/hive:2.3.2-postgresql-metastore
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
    image: bde2020/hive:2.3.2-postgresql-metastore
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
    image: bde2020/spark-master:2.4.0-hadoop2.7
    container_name: spark-master
    environment:
      - INIT_DAEMON_STEP=spark
      - 'constraint:node==master'
    ports:
      - 8080:8080
    networks:
      - hadoop

  spark-worker:
    image: bde2020/spark-worker:2.4.0-hadoop2.7
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
