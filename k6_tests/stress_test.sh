# TEST localhost:8080 service
##################################
export BASE_URL="http://localhost:8080/"


# if you want to run a specific test
# must be sent as parameter 1
TEST_TO_RUN=$1

# by default (no params) all test are run (runAll)
if [ -z "$1" ]
  then
    TEST_TO_RUN="./src/tests/runAll.test.js"
fi

#  40 virtual users
#  starts with 40 concurrent requests
#  and stays for 10 mins with concurrent requests100

 k6 run -s 1m:40 \
        -s 1m:100 \
        -s 10m:100 \
       $TEST_TO_RUN


