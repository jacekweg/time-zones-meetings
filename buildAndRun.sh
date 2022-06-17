#!/bin/sh
mvn clean package && docker build -t pl.polsl/TimeZones .
docker rm -f TimeZones || true && docker run -d -p 9080:9080 -p 9443:9443 --name TimeZones pl.polsl/TimeZones