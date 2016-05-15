(function() {
    'use strict';

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider'];

    function Config($routeProvider) {
        $routeProvider
            .when('/grafiek/stroom/opgenomen-vermogen', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })

            .when('/grafiek/klimaat/:soort', {
                templateUrl: 'app/klimaat-historie/klimaat-historie.html',
                controller: 'KlimaatSensorGrafiekController'
            })
            .when('/grafiek/:soort/uur', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'UurEnergieGrafiekController'
            })
            .when('/grafiek/:soort/dag', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'DagEnergieGrafiekController'
            })
            .when('/grafiek/:soort/maand', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'MaandEnergieGrafiekController'
            })

            .when('/meterstanden', {
                templateUrl: 'app/meterstanden/meterstanden.html',
                controller: 'MeterstandenController'
            })
            .when('/kosten', {
                templateUrl: 'app/beheer/kosten.html',
                controller: 'KostenController'
            })
            .when('/mindergasnl', {
                templateUrl: 'app/beheer/mindergasnl.html',
                controller: 'MindergasnlController'
            })
            .when('/', {
                templateUrl: 'app/dashboard/dashboard.html',
                controller: 'DashboardController'
            })
            .otherwise({redirectTo: 'app/dashboard/dashboard.html'});
    }
})();