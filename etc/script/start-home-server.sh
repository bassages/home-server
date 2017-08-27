#!/bin/bash
#cd into directory where this script is located
cd "$(dirname "$0")"
exec java -Duser.timezone=Europe/Amsterdam -Xms416m -Xmx480m -jar home-server-*.jar
