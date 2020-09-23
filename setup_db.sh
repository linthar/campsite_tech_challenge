echo "Starting a new MySQL server instance on localhost:30306 with admin user/pass: root/root_pass"
docker run --name mysqlCampsite -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root_pass -e MYSQL_DATABASE=campsite -e MYSQL_USER=campsite_user -e MYSQL_PASSWORD=campsite_pass -d mysql:5.7

echo " Creating Campsite DB and service user..."
docker exec mysqlCampsite sh -c "exec mysql -uroot -proot_pass -e \"CREATE USER 'campsite_user' IDENTIFIED BY 'campsite_pass'\""
docker exec mysqlCampsite sh -c "exec mysql -uroot -proot_pass -e \"CREATE DATABASE campsite_socio\""
docker exec mysqlCampsite sh -c "exec mysql -uroot -proot_pass -e \"GRANT ALL ON historia_clinica_socio.* TO 'campsite_user'\""
docker exec mysqlCampsite sh -c "exec mysql -uroot -proot_pass -e \"FLUSH PRIVILEGES\""


echo "creating the DB Tables for campsite service"

export JDBC_USER=campsite_user
export JDBC_PASSWORD=campsite_pass
export JDBC_URL=jdbc:mysql://127.0.0.1:3306/campsite

./gradlew database:liquibase update


