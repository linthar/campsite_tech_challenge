#!/bin/bash

export SERVER_PORT=8080
export ROOT_LOGGING_LEVEL=DEBUG
export UPG_LOGGING_LEVEL=DEBUG

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite
export JDBC_DRIVER=com.mysql.cj.jdbc.Driver
export MAX_DB_POOL_SIZE=10

if [ "$1" != "--fast" ]; then
    echo "--- REBUILD --- "
    rm ./service/build/libs/service*.jar
    # force cleanup
    ./gradlew service:clean build
fi


docker start mysqlCampsite

# starts the jar service in debug mode
java -jar -Dmicronaut.environments=dev -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 ./service/build/libs/service-all.jar