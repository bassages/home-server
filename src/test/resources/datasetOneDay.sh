#!/usr/bin/env bash
DATE=1445292000000
WATT=80
UP=1

for i in {0..96}
do
    if [ "$WATT" -eq "1080" ]
      then
        UP=0
    fi

    if [ "$UP" -eq "1" ]
      then
        WATT=$(($WATT + 20))
    else
        WATT=$(($WATT - 20))
    fi

    curl -X POST --header "Content-Type: application/x-www-form-urlencoded" --header "Accept: application/json" -d "vermogenInWatt=$WATT&datumtijd=$DATE" "http://localhost:8080/homecontrol/rest/elektriciteit/opgenomenVermogen"
    DATE=$(($DATE + 900000))
done