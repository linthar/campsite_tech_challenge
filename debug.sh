#!/bin/bash

export SERVER_PORT=8080
export LOGGING_LEVEL=DEBUG

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite
export MAX_DB_POOL_SIZE=10

if [ "$1" != "--fast" ]; then
    echo "--- REBUILD --- "
    rm ./service/build/libs/service*.jar
    # force cleanup
    ./gradlew service:clean build
fi


# starts the jar service in debug mode 

java -jar -Dmicronaut.environments=dev -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6010 ./service/build/libs/service-all.jar