#!/bin/bash
export SERVER_PORT=8080
export ROOT_LOGGING_LEVEL=INFO
export CAMPSITE_LOGGING_LEVEL=DEBUG

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite
export JDBC_DRIVER=com.mysql.cj.jdbc.Driver
export MAX_DB_POOL_SIZE=10
export REDIS_URI=redis://localhost

if [ "$1" = "--clean" ]; then
    ./gradlew service:clean
fi

docker start mysqlCampsite
docker start redisCampsite


./gradlew service:run

