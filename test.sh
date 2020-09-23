## test
./gradlew clean test jacocoTestReport



WEB_BROWSER=firefox
DIR=`pwd`
REPORT_URL="$DIR/service/build/jacocoHtml/index.html"

echo "open test coverage report URL in WebBrowser "
echo "$WEB_BROWSER  $REPORT_URL"
$WEB_BROWSER  $REPORT_URL

