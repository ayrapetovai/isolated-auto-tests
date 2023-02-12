## db
run postgres
 ```shell
docker run \
    -p 5432:5432 \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD=postgres \
        -e PGDATA=/var/lib/postgresql/json-data/pgdata \
        --mount source=postgres_db_volume,target=/var/lib/postgresql/json-data \
        postgres &> /dev/null
```
