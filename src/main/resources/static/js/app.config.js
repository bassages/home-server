(function() {
    'use strict';

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider'];

    function Config($routeProvider) {
        $routeProvider
            .when('/grafiek/stroom/opgenomen-vermogen', {
                templateUrl: 'grafiek.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })

            .when('/grafiek/:soort/uur', {
                templateUrl: 'grafiek.html',
                controller: 'UurGrafiekController'
            })
            .when('/grafiek/:soort/dag', {
                templateUrl: 'grafiek.html',
                controller: 'DagGrafiekController'
            })
            .when('/grafiek/:soort/maand', {
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
                controller: 'DashboardController'
            })
            .otherwise({redirectTo: 'dashboard.html'});
    }
})();