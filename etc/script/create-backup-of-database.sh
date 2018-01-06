#!/bin/bash
echo "Stop service"
sudo service home-server stop

echo "Wait until service stopped"
sleep 10

echo "Copy database"
today=`date +%Y%m%d%H%M%S`
cp -R database home-server.database.backup.${today}

echo "Start service"
sudo service home-server start
