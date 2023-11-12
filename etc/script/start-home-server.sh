#!/bin/bash
exec /opt/jdk-21.0.1+12/bin/java -Duser.timezone=Europe/Amsterdam -Xms512m -Xmx600m -jar home-server*.jar
