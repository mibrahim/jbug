#!/bin/bash
git pull; mvn clean package -DskipTests -Djetty.port=9010 jetty:run
