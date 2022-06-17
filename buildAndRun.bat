@echo off
call mvn clean package
call docker build -t pl.polsl/TimeZones .
call docker rm -f TimeZones
call docker run -d -p 9080:9080 -p 9443:9443 --name TimeZones pl.polsl/TimeZones