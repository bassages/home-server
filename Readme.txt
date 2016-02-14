Backlog

---------------

Mindergas.nl upload

---------------

Gasverbruik grafieken

---------------

Data generator/upload module

---------------

Klik op dag in grafiek -> ga naar dag overzicht

----------------

Vergelijken met andere jaren (in maand overview)

----------------

Grafiek datum selectie in URL

----------------

Date range:

http://eternicode.github.io/bootstrap-datepicker/?markup=range&format=dd-mm-yyyy&weekStart=&startDate=&endDate=0d&startView=0&minViewMode=0&maxViewMode=2&todayBtn=linked&clearBtn=false&language=nl&orientation=bottom+left&multidate=&multidateSeparator=&daysOfWeekHighlighted=0&daysOfWeekHighlighted=6&calendarWeeks=on&autoclose=on&todayHighlight=on&keyboardNavigation=on#sandbox
http://stackoverflow.com/questions/30481941/bootstrap-datepicker-disable-past-date-after-first-is-set

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

