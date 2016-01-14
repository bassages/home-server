'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('appHomecontrol', [
    'ngRoute',
    'ngResource',
    'appHomecontrol.kostenService',
    'appHomecontrol.kostenController',
    'appHomecontrol.meterstandService',
    'appHomecontrol.uurGrafiekController',
    'appHomecontrol.dagGrafiekController',
    'appHomecontrol.maandGrafiekController',
    'appHomecontrol.stroomMeterstandController',
    'appHomecontrol.localizationService',
    'appHomecontrol.grafiekWindowSizeService',
    'appHomecontrol.sharedDataService'

]).config(['$routeProvider', function($routeProvider) {

        $routeProvider
            .when('/grafiek/:energiesoort/uur', {
                templateUrl : 'grafiek.html',
                controller: 'UurGrafiekController'
            })
            .when('/grafiek/:energiesoort/dag', {
                templateUrl : 'grafiek.html',
                controller: 'DagGrafiekController'
            })
            .when('/grafiek/:energiesoort/maand', {
                templateUrl : 'grafiek.html',
                controller: 'MaandGrafiekController'
            })
            .when('/kosten', {
                templateUrl : 'kosten.html',
                controller: 'KostenController'
            })
            .when('/', {
                templateUrl : 'dashboard.html',
                controller: 'StroomMeterstandController'
            })
            .otherwise({redirectTo: 'dashboard.html'});
}]);

app.filter('dateWithoutDayName', function($filter) {
    return function(input) {
        if(input == null){ return ""; }
        var _date = $filter('date')(new Date(input), 'dd-MM-yyyy');
        return _date.toUpperCase();

    };
});

app.directive('formatteddate', function() {
    return {
        restrict: 'A', // only matches attribute
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
            function fromUser(text) {
                return Date.parse(text);
            }
            function toUser(date) {
                return date.toString(scope.getDateFormat());
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});
