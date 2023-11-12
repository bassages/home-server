#!/bin/bash
set -e

latest_github_release=$(curl -s https://api.github.com/repos/bassages/home-server/releases/latest)
latest_github_release_version=$(echo "$latest_github_release" | jq -r '.name // ""')
latest_github_release_asset_download_url=$(echo "$latest_github_release" | jq -r '.assets[0].browser_download_url // ""')
latest_github_release_asset_name=$(echo "$latest_github_release" | jq -r '.assets[0].name // ""')

if [[ "$latest_github_release_version" ]]
then
  echo "[INFO] Latest release version: $latest_github_release_version"
else
  echo "[ERROR] No latest release found"
  exit 1
fi

if [[ "$latest_github_release_asset_download_url" ]]
then
  echo "[INFO] Latest release asset download url: $latest_github_release_asset_download_url"
else
  echo "[ERROR] No latest release version download URL found"
  exit 1
fi

echo "[INFO] Latest release asset name: $latest_github_release_asset_name"

echo "[INFO] Downloading latest release asset from GitHub"
local_release_download=releases/${latest_github_release_asset_name}
curl "${latest_github_release_asset_download_url}" --create-dirs -L -o ${local_release_download}

echo "[INFO] Stopping service"
sudo systemctl stop home-server

echo "[INFO] Updating symbolic link to ${local_release_download}"
ln -sf "${local_release_download}" home-server.jar

echo "[INFO] Wait 5 seconds to make sure that the service is stopped"
sleep 5s

echo "[INFO] Starting service"
sudo systemctl start home-server
