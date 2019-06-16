# funexchange
Simple open source sandbox of stock exchange with web client.

How to start:
1) Build project - mvn install
2) Start server -  mvn exec:java -f fun-exchange-server\pom.xml
3) Start simple web server for client - mvn gwt:run -f fun-exchange-web\pom.xml
4) Open in browser - http://127.0.0.1:8888/WebClient.html
