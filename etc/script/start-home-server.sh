#!/bin/bash
#cd into directory where this script is located
cd "$(dirname "$0")"
exec /opt/jdk-19.0.1/bin/java -Duser.timezone=Europe/Amsterdam -Xms512m -Xmx600m -jar home-server*.jar
