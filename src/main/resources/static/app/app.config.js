(function() {
    'use strict';

    angular.module('app', [
        'ngRoute',
        'ngResource',
        'ngAnimate',
        'ui.bootstrap'
    ]);

    angular
        .module('app')
        .constant('DATETIME_CONSTANTS', {
                shortMonths : ["Jan.", "Feb.", "Maa.", "Apr.", "Mei.", "Jun.", "Jul.", "Aug.", "Sep.", "Okt.", "Nov.", "Dec."],
                shortDays : ["Zo.", "Ma.", "Di.", "Wo.", "Do.", "Vr.", "Za."],
                fullMonths : ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"]
            }
        );

    // Init Lodash
    angular.module('app')
        // allow DI for use in controllers, unit tests
        .constant('_', window._)

        // FAILES AFTER UGLIFY / MINIFY:
        // use in views, ng-repeat="x in _.range(3)"
        // .run(function ($rootScope) {
        //     $rootScope._ = window._;
        // })
    ;

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider', '$compileProvider', 'DATETIME_CONSTANTS'];

    function Config($routeProvider, $compileProvider, DATETIME_CONSTANTS) {
        // See https://docs.angularjs.org/guide/production
        $compileProvider.debugInfoEnabled(false);
        $compileProvider.commentDirectivesEnabled(false);
        $compileProvider.cssClassDirectivesEnabled(false);

        numbro.culture('nl-NL');

        var d3Formatters = d3.locale({
            "decimal": ",",
            "thousands": ".",
            "grouping": [3],
            "currency": ["€", ""],
            "dateTime": "%a %b %e %X %Y",
            "date": "%d-%m-%Y",
            "time": "%H:%M:%S",
            "periods": ["AM", "PM"],
            "days": ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
            "shortDays": DATETIME_CONSTANTS.shortDays,
            "months": DATETIME_CONSTANTS.fullMonths,
            "shortMonths": DATETIME_CONSTANTS.shortMonths
        });
        d3.time.format = d3Formatters.timeFormat;
        d3.format = d3Formatters.numberFormat;

        $routeProvider
            .when('/energie/stroom/opgenomen-vermogen', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })
            .when('/klimaat/historie/:sensortype', {
                templateUrl: '/app/klimaat/klimaat-historie.html',
                controller: 'KlimaatHistorieController'
            })
            .when('/klimaat/top-charts/:sensortype', {
                templateUrl: '/app/klimaat/klimaat-top-charts.html',
                controller: 'KlimaatTopChartsController',
                controllerAs: 'vm'
            })
            .when('/klimaat/average/:sensortype', {
                templateUrl: '/app/klimaat/klimaat-average.html',
                controller: 'KlimaatAverageController',
                controllerAs: 'vm'
            })

            .when('/energie/:verbruiksoort/uur', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'UurEnergieHistorieController'
            })
            .when('/energie/:verbruiksoort/dag', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'DagEnergieHistorieController'
            })
            .when('/energie/:verbruiksoort/maand', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'MaandEnergieHistorieController'
            })

            .when('/energie/meterstanden', {
                templateUrl: '/app/meterstanden/meterstanden.html',
                controller: 'MeterstandenController',
                controllerAs: 'vm'
            })
            .when('/energiecontracten', {
                templateUrl: '/app/beheer/energiecontracten.html',
                controller: 'EnergieContractenController',
                controllerAs: 'vm'
            })
            .when('/mindergasnl', {
                templateUrl: '/app/beheer/mindergasnl.html',
                controller: 'MindergasnlController',
                controllerAs: 'vm'
            })
            .when('/application-settings', {
                templateUrl: '/app/beheer/application-settings.html',
                controller: 'ApplicationSettingsController',
                controllerAs: 'vm'
            })
            .when('/', {
                templateUrl: '/app/dashboard/dashboard.html',
                controller: 'DashboardController',
                controllerAs: 'vm'
            })
            .otherwise({redirectTo: '/index.html'});
    }
})();