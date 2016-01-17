Backlog

---------------

Kosten:
- hernoem 'van' naar 'vanaf'
- Verwijder totEnMet

---------------

CRUD kosten
- Confirm delete (https://github.com/m-e-conroy/angular-dialog-service)

---------------

Laatste meterstanden per dag

----------------

Angular bootstrap

Hmmm....
- Datepicker kan niet alleen de clear button verwijderen.

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

