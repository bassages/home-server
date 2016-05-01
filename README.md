# Features
- Ontvangen (via JSON berichten) van slimme meter data en deze opslaan in een database
- Ontvangen (via JSON berichten) van klimaat sensor data (temperatuur en luchtvochtigheid) en deze opslaan in een database
- Beschikbaar stellen van de slimme meter data via een (reponsive) web interface
- Beschikbaar stellen van de klimaat sensor data via een (reponsive) web interface
- Mogelijkheid om gas meterstanden automatisch te uploaden naar mindergas.nl

# Gebruikte technologie
- Spring Boot (Java 8, maven)
- Angular (HTML, javascript)
- Bootstrap (HTML, CSS)
- Runtime: OpenShift DIY en MySQL Cartridges

# Screenshots

## Actueel
![Alt text](screenshots/actueel-xl.png?raw=true "Actueel")

## Kosten per maand in jaar
![Alt text](screenshots/kosten-maand-xl.png?raw=true "Kosten per maand in jaar")

## Verbruik per dag in maand
![Alt text](screenshots/verbruik-dag-xl.png?raw=true "Verbruik per dag in maand")

## Meterstanden per dag
![Alt text](screenshots/meterstanden-xl.png?raw=true "Meterstanden per dag")

## Temperatuur
![Alt text](screenshots/temperatuur.png?raw=true "Temperatuur")

## Kosten per maand in jaar (klein beeldscherm)
<img src="https://raw.githubusercontent.com/bassages/home-server/master/screenshots/kosten-maand-xs.png" width="400">

# Backlog

---------------

Access control

---------------

// BUG for chrome: https://groups.google.com/forum/#!topic/c3js/0BrndJqBHak
graphConfig.data.onclick = function (d, element) {
    $log.info('d: ' + JSON.stringify(d));
    $log.info('element: ' + element);
};

---------------

Gasverbruik widget leds op dashboard

---------------

Vergelijken met andere jaren (in maand overview)

----------------

Directive voor datepicker op grafiek pagina

----------------

Gebruik en kosten per leverancier

var chart = c3.generate({
    data: {
        type: 'bar',
        json: [
                {"Essent":314, "GreenChoice":68, "maand":2},
                {"Essent":610, "GreenChoice":132, "maand":3},
            ],
        groups: [
            ['Essent', 'GreenChoice']
        ],
        keys:  {x: 'maand', value: ['Essent', 'GreenChoice']}
    }
});

