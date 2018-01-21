<a href="https://travis-ci.org/bassages/home-server" taget="_blank"><img src="https://travis-ci.org/bassages/home-server.svg?branch=master" alt="Travis CI badge"></a>
<a href="https://sonarcloud.io/dashboard?id=home-server" target="_blank"><img src="https://sonarcloud.io/api/badges/gate?key=home-server" alt="SonarCloud"></a>

# Features
- Ontvangen van slimme meter data en deze opslaan in een database
- Ontvangen van klimaat sensor data (temperatuur en luchtvochtigheid) en deze opslaan in een database
- Beschikbaar stellen van de slimme meter data via een (reponsive) web interface
- Beschikbaar stellen van de klimaat sensor data via een (reponsive) web interface
- Mogelijkheid om gas meterstanden dagelijks automatisch te uploaden naar mindergas.nl

# Technologie
- Spring Boot (Java 8, Gradle)
- AngularJS (NPM, Gulp, Less)
- Bootstrap
- C3.js voor grafieken
- Runtime: Raspberry PI 3

# Screenshots

## Actueel
![Alt text](documentation/screenshots/actueel-xl.png?raw=true "Actueel")

## Opgenomen vermogen
![Alt text](documentation/screenshots/opgenomen-vermogen.png?raw=true "Actueel")

## Kosten per maand in jaar
![Alt text](documentation/screenshots/kosten-maand-xl.png?raw=true "Kosten per maand in jaar")

## Verbruik per dag in maand
![Alt text](documentation/screenshots/verbruik-dag-xl.png?raw=true "Verbruik per dag in maand")

## Meterstanden per dag
![Alt text](documentation/screenshots/meterstanden-xl.png?raw=true "Meterstanden per dag")

## Temperatuur / Luchtvochtigheid
![Alt text](documentation/screenshots/temperatuur.png?raw=true "Temperatuur")

## Kosten per maand in jaar (klein scherm)
<img src="https://raw.githubusercontent.com/bassages/home-server/master/documentation/screenshots/kosten-maand-xs.png" width="400">
