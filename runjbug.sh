#!/bin/bash
git pull; mvn package -DskipTests -Djetty.port=9010 jetty:run
