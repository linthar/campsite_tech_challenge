# campsite_tech_challenge

see /docs for specifications

Requeriments

- docker
- jdk 11
- K6: To run stress tests (https://k6.io/)  (https://k6.io/docs/getting-started/installation) 

# Test Coverage Report
````
./test.sh 
````




# Service Execution

## Docker servers setup
 First you have execute:
```
./setup.sh
```
 This script starts a new MySQL instance on localhost:30306 with admin user/pass: root/root_pass  and a REDIS server in localhost:6379


#### MySQL start/stop

 After setup is done you can start/stop MySQL server using the commands:

```
docker stop mysqlCampsite
docker start mysqlCampsite
```
#### REDIS start/stop

 After setup is done you can start/stop REDIS server using the commands:

```
docker start redisCampsite
docker stop redisCampsite
```


## Running the service

````
./run.sh
````
Campsite service should be available at port 8080

### API check 

If the service started ok, this curl must return HTTP 200

````
curl -I -s -L 'http://0.0.0.0:8080/availability' | grep "HTTP/1.1"
````
expected result is:
    
````
HTTP/1.1 200 OK
````


### SWAGGER doc:

http://localhost:8080/swagger/views/swagger-ui/


## Running the stress test

````
./k6_tests/stress_test.sh 
````
This script starts a K6 test that get the availability report from localhost:8080.
Uses up to 100 "Virtual Users" concurrently and continually hitting the API.





