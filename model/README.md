```
docker exec -it cassandra bash
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
```
