#!/bin/bash

if [[ ! "$1" ]]
then
  echo "[ERROR] Please specify the release to install as parameter. Example: home-server-v1.30.0.jar"
  exit 1
fi

release=releases/$1

if [[ ! -f $release ]]
then
  echo "[ERROR] Cannot find file ${release}. The following files are available:"
  ls -l releases
  exit 1
fi

echo "[INFO] Stopping service"
sudo service home-server stop

echo "[INFO] Updating symbolic link to ${release}"
ln -sf "${release}" home-server.jar

echo "[INFO] Wait 20 seconds until service is stopped"
sleep 20s

echo "[INFO] Starting service"
sudo service home-server start
