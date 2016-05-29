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
                controller: 'KlimaatHistorieController'
            })
            .when('/grafiek/:soort/uur', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'UurEnergieHistorieController'
            })
            .when('/grafiek/:soort/dag', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'DagEnergieHistorieController'
            })
            .when('/grafiek/:soort/maand', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'MaandEnergieHistorieController'
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