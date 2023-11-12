#!/bin/bash
set -e

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
sudo systemctl stop home-server

echo "[INFO] Updating symbolic link to ${release}"
ln -sf "${release}" home-server.jar

echo "[INFO] Wait 5 seconds to make sure that the service is stopped"
sleep 5s

echo "[INFO] Starting service"
sudo systemctl home-server start
