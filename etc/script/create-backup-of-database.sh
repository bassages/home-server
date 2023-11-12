#!/bin/bash
set -e

echo "Stop service"
sudo systemctl stop home-server

echo "Copy database"
today=`date +%Y%m%d%H%M%S`
zip -r -dd home-server.database.backup.${today}.zip database/

echo "Start service"
sudo systemctl start home-server
