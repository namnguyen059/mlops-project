```bash
docker-compose down
```

```bash
docker network create hadoop

docker-compose up -d
```

```bash
docker cp /Users/nguyennam/Desktop/Mlops/house_prices.csv namenode:/house_prices.csv
```

```bash
docker exec -it namenode /bin/bash
hdfs dfs -mkdir /data
hdfs dfs -put /house_prices.csv /data/house_prices.csv
hdfs dfs -mkdir -p /user/hive/warehouse
hdfs dfs -chmod -R 775 /user/hive/warehouse
hdfs dfs -chown -R spark:supergroup /user/hive/warehouse
exit  
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

hadoop-hive.env: https://github.com/big-data-europe/docker-hive/blob/master/hadoop-hive.env
