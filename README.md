# Actueel
![Alt text](screenshots/actueel-xl.png?raw=true "Actueel")

# Kosten per maand in jaar
![Alt text](screenshots/kosten-maand-xl.png?raw=true "Kosten per maand in jaar")

# Verbruik per dag in maand
![Alt text](screenshots/verbruik-dag-xl.png?raw=true "Verbruik per dag in maand")

# Meterstanden per dag
![Alt text](screenshots/meterstanden-xl.png?raw=true "Meterstanden per dag")

# Kosten per maand in jaar (klein beeldscherm)
<img src="https://raw.githubusercontent.com/bassages/home-server/master/screenshots/kosten-maand-xs.png" width="400">

**Backlog**

---------------

Access control

---------------

Mindergas.nl upload

---------------

// BUG for chrome: https://groups.google.com/forum/#!topic/c3js/0BrndJqBHak
graphConfig.data.onclick = function (d, element) {
    $log.info('d: ' + JSON.stringify(d));
    $log.info('element: ' + element);
};

---------------

Gasverbruik widget leds op dashboard

---------------

Data generator/upload module

---------------

Vergelijken met andere jaren (in maand overview)

----------------

Grafiek datum selectie in URL

----------------

Angular bootstrap

Hmmm....
- Datepicker kan niet alleen de clear button verwijderen.

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

