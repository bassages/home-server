# home-server

![Build home-server](https://github.com/bassages/home-server/workflows/Build%20home-server/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=coverage)](https://sonarcloud.io/dashboard?id=home-server)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=code_smells)](https://sonarcloud.io/dashboard?id=home-server)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=bugs)](https://sonarcloud.io/dashboard?id=home-server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=home-server)

## Features
- Provides a RESTful API to store and query data related to a [smart meter](https://en.wikipedia.org/wiki/Smart_meter)
- Provides a RESTful API to store and query data related to environmental sensor readings (temperature and humidity)
- Ability to send gas meter readings to [mindergas.nl](https://mindergas.nl) on a daily basis

## Links
* [home-sensors](https://github.com/bassages/home-sensors)
* [home-frontend](https://github.com/bassages/home-frontend)

### Tools

KeyStore Explorer is an open source GUI replacement for the Java command-line utilities keytool and jarsigner. 
https://keystore-explorer.org/

## Common development tasks

### Update versions

#### Gradle
For this, you can use the following command: `gradlew wrapper --gradle-version <new version>`

### Dependencies
For this, you can use the following command: `gradlew dependencyUpdates`.
Inspect the output of the command, update build.gradle manually and test if the application still builds and executes correctly.

### JDK
On the Raspberry PI, the *Liberica JDK* distribution is used.
See the [downloads page](https://bell-sw.com/pages/downloads/) and look for "Linux" :arrow_right: "ARM" :arrow_right: ".tar.gz".
On the RPI:
- download it: `cd /opt`, `sudo wget <URL of the file to be downloaded>`
- unpack it (`sudo tar xvfz <name of the file>.tar.gz`)
- update [./etc/script/start-home-server.sh]

## Compact database file
1. Login in to the application
2. Go to the H2 console, eg:
   ```
   http://home:9090/h2-console/
   ```
3. Connect with the following connection string
   ```
   jdbc:h2:~/home-server/database/home-server-database;DB_CLOSE_ON_EXIT=FALSE
   ```
4. Execute the following SQL statement:
   ```   
   SHUTDOWN COMPACT
   ```