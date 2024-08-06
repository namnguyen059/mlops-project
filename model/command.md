```
docker exec -it cassandra cqlsh
```

```
CREATE KEYSPACE model_repo WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

USE model_repo;

CREATE TABLE models (
    model_id UUID PRIMARY KEY,
    model_name text,
    model_version text,
    model_file blob,
    created_at timestamp
);

SELECT model_id, created_at FROM models;
```
```
docker exec -it mlops-hive-server-1 /bin/bash
hive
SHOW TABLES;
SELECT COUNT(price) FROM default.house_price_predictions LIMIT 10;
```

```
docker build -t mxomtm/house-price-prediction:latest -f Dockerfile .
docker push mxomtm/house-price-prediction:latest  ```
```

```
kubectl create ns model-serving
kubens model-serving
helm upgrade --install app \
    --set image.repository=mxomtm/house-price-prediction \
    --set image.tag=latest \
    /Users/nguyennam/Desktop/Mlops/model/helm_charts/app\
    --namespace model-serving
```

```
curl -X GET http://localhost:80/predict
```

```
cd helm_charts/prometheus-operator-crds
kubectl create ns monitoring
kubens monitoring
helm upgrade --install prometheus-crds .
```

```
cd helm_charts/prometheus
kubens monitoring
helm upgrade --install prometheus .
```

```
cd helm_charts/grafana
kubens monitoring
helm upgrade --install grafana .
```
```
kubectl get nodes -o wide
```

Dashboard ID: 1860
