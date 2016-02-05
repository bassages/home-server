(function() {
    'use strict';

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider'];

    function Config($routeProvider) {
        $routeProvider
            .when('/grafiek/:energiesoort/uur', {
                templateUrl: 'grafiek.html',
                controller: 'UurGrafiekController'
            })
            .when('/grafiek/:energiesoort/dag', {
                templateUrl: 'grafiek.html',
                controller: 'DagGrafiekController'
            })
            .when('/grafiek/:energiesoort/maand', {
                templateUrl: 'grafiek.html',
                controller: 'MaandGrafiekController'
            })
            .when('/meterstanden', {
                templateUrl: 'meterstanden.html',
                controller: 'MeterstandenController'
            })
            .when('/kosten', {
                templateUrl: 'kosten.html',
                controller: 'KostenController'
            })
            .when('/', {
                templateUrl: 'dashboard.html',
                controller: 'StroomMeterstandController'
            })
            .otherwise({redirectTo: 'dashboard.html'});
    }
})();