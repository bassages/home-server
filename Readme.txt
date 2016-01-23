Backlog

---------------

Weekpicker: https://github.com/eternicode/bootstrap-datepicker/issues/615

---------------

CRUD kosten
- Maak vanaf uniek + validatie

---------------

Laatste meterstanden per dag

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

