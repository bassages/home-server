#!/bin/bash
exec /opt/graalvm-community-openjdk-25.0.1+8.1/bin/java -Duser.timezone=Europe/Amsterdam -Xms512m -Xmx512m -jar home-server*.jar
