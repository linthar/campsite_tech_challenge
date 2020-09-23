# campsite_tech_challenge

see /docs for specifications


# Service Setup

## MySQL setup
 First you have to start a MySQL Docker image in you host, to do that, execute:
 this command starts a new MySQL instance on localhost:30306 with admin user/pass: root/root_pass

```
./setup_db.sh
```


#### MySQL start/stop

 After MySQL setup is done you can start stop the DB using the commands:

```
docker stop mysqlCampsite
docker start mysqlCampsite
```



# Running the service

````
./run.sh
````

### API check 

If the service started ok, this curl must return HTTP 200

````
curl -I -s -L 'http://0.0.0.0:8080/health' | grep "HTTP/1.1"
````




# Testing the service

````
./test.sh
````



## Endpoints:
-POST   /reserve
-DELETE /reserve/{reservation_id}
-PATCH  /reserve/{reservation_id}
-GET    /availability



## Ejemplos cURL


### 
