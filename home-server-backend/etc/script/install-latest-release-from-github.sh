#!/bin/bash
latest_github_release=$(curl -s https://api.github.com/repos/bassages/home-server/releases/latest)
latest_github_release_version=$(echo "$latest_github_release" | jq -r '.name // ""')
latest_github_release_asset_download_url=$(echo "$latest_github_release" | jq -r '.assets[0].browser_download_url // ""')
latest_github_release_asset_name=$(echo "$latest_github_release" | jq -r '.assets[0].name // ""')

if [ "$latest_github_release_version" ]
then
  echo "[INFO] Latest release version: $latest_github_release_version"
  echo "[INFO] Latest release asset download url: $latest_github_release_asset_download_url"
  echo "[INFO] Latest release asset name: $latest_github_release_asset_name"

  echo "[INFO] Downloading latest release asset from GitHub"
  local_release_download=releases/${latest_github_release_asset_name}
  curl ${latest_github_release_asset_download_url} --create-dirs -L -o ${local_release_download}

  echo "[INFO] Stopping service"
  sudo service home-server stop

  echo "[INFO] Updating symbolic link to ${local_release_download}"
  ln -sf ${local_release_download} home-server.jar

  echo "[INFO] Starting service"
  sudo service home-server start
else 
  echo "[ERROR] No latest release found"
fi
