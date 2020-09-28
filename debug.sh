#!/bin/bash

export SERVER_PORT=8080
export ROOT_LOGGING_LEVEL=DEBUG
export CAMPSITE_LOGGING_LEVEL=DEBUG

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite
export JDBC_DRIVER=com.mysql.cj.jdbc.Driver
export MAX_DB_POOL_SIZE=10
export REDIS_URI=redis://localhost

if [ "$1" != "--fast" ]; then
    echo "--- REBUILD --- "
    # forces a clean build just in case
    ./gradlew service:clean build
fi


docker start mysqlCampsite
docker start redisCampsite


# starts the jar service in debug mode
java -jar -Dmicronaut.environments=dev -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 ./service/build/libs/service-all.jar