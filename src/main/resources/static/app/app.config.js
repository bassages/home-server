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
        .config(Config);

    Config.$inject = ['$routeProvider', '$compileProvider'];

    function Config($routeProvider, $compileProvider) {
        // See https://docs.angularjs.org/guide/production
        $compileProvider.debugInfoEnabled(false);
        $compileProvider.commentDirectivesEnabled(false);
        $compileProvider.cssClassDirectivesEnabled(false);

        $routeProvider
            .when('/energie/stroom/opgenomen-vermogen', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })
            .when('/klimaat/grafiek/:sensortype', {
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
            .when('/energie/:soort/grafiek/uur', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'UurEnergieHistorieController'
            })
            .when('/energie/:soort/grafiek/dag', {
                templateUrl: '/app/energie-historie/energie-historie.html',
                controller: 'DagEnergieHistorieController'
            })
            .when('/energie/:soort/grafiek/maand', {
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
            .when('/bedien', {
                templateUrl: '/app/bedien/bedien.html',
                controller: 'BedienController',
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