(function() {
    'use strict';

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider'];

    function Config($routeProvider) {
        $routeProvider
            .when('/grafiek/stroom/opgenomen-vermogen', {
                templateUrl: 'energie-grafiek.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })

            .when('/grafiek/klimaat/temperatuur', {
                templateUrl: 'klimaat-sensor-grafiek.html',
                controller: 'KlimaatSensorGrafiekController'
            })
            .when('/grafiek/:soort/uur', {
                templateUrl: 'energie-grafiek.html',
                controller: 'UurEnergieGrafiekController'
            })
            .when('/grafiek/:soort/dag', {
                templateUrl: 'energie-grafiek.html',
                controller: 'DagEnergieGrafiekController'
            })
            .when('/grafiek/:soort/maand', {
                templateUrl: 'energie-grafiek.html',
                controller: 'MaandEnergieGrafiekController'
            })

            .when('/meterstanden', {
                templateUrl: 'meterstanden.html',
                controller: 'MeterstandenController'
            })
            .when('/kosten', {
                templateUrl: 'kosten.html',
                controller: 'KostenController'
            })
            .when('/mindergasnl', {
                templateUrl: 'mindergasnl.html',
                controller: 'MindergasnlController'
            })
            .when('/', {
                templateUrl: 'dashboard.html',
                controller: 'DashboardController'
            })
            .otherwise({redirectTo: 'dashboard.html'});
    }
})();