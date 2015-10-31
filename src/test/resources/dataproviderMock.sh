#!/bin/bash
for i in {0..150}
do
    now=$(($(date +'%s * 1000 + %-N / 1000000')))
    watt=$(($i * 10))
    curl -X POST --header "Content-Type: application/x-www-form-urlencoded" --header "Accept: application/json" -d "vermogenInWatt=$watt&dt=$now" "http://localhost:8080/homecontrol/rest/elektriciteit/opgenomenVermogen"
    sleep 1
done

COUNT=150
while [ $COUNT -gt 0 ]; do
    now=$(($(date +'%s * 1000 + %-N / 1000000')))
    watt=$(($COUNT * 10))
    curl -X POST --header "Content-Type: application/x-www-form-urlencoded" --header "Accept: application/json" -d "vermogenInWatt=$watt&dt=$now" "http://localhost:8080/homecontrol/rest/elektriciteit/opgenomenVermogen"
    sleep 1
    let COUNT=COUNT-1
done

now=$(($(date +'%s * 1000 + %-N / 1000000')))
curl -X POST --header "Content-Type: application/x-www-form-urlencoded" --header "Accept: application/json" -d "vermogenInWatt=0&dt=$now" "http://localhost:8080/homecontrol/rest/elektriciteit/opgenomenVermogen"
