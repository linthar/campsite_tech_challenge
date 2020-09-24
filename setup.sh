
echo "Deleting previous MySQL server instance on localhost  (if there is one)"
docker stop mysqlCampsite  > /dev/null 2>&1
docker rm mysqlCampsite  > /dev/null 2>&1



echo " "
echo " "
echo "Starting a new MySQL server instance on localhost:30306 with admin user/pass: root/root_pass"
docker run --name mysqlCampsite -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root_pass -e MYSQL_DATABASE=campsite -e MYSQL_USER=campsite_user -e MYSQL_PASSWORD=campsite_pass -d mysql:5.7

# wait the server to be ready
sleep 20

echo " "
echo " "
echo "creating the DB Tables for campsite service"

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite

./gradlew database:liquibase update


