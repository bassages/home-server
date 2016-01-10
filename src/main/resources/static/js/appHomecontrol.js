'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('appHomecontrol', [
    'ngRoute',
    'appHomecontrol.meterstandService',
    'appHomecontrol.uurGrafiekController',
    'appHomecontrol.dagGrafiekController',
    'appHomecontrol.maandGrafiekController',
    'appHomecontrol.stroomMeterstandController',
    'appHomecontrol.localizationService',
    'appHomecontrol.grafiekWindowSizeService',
    'appHomecontrol.sharedDataService'
]).config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/grafiek/:energiesoort/uur', {
            templateUrl : 'grafiek.html',
            controller: 'UurGrafiekController'
        });
        $routeProvider.when('/grafiek/:energiesoort/dag', {
            templateUrl : 'grafiek.html',
            controller: 'DagGrafiekController'
        });
        $routeProvider.when('/grafiek/:energiesoort/maand', {
            templateUrl : 'grafiek.html',
            controller: 'MaandGrafiekController'
        });
        $routeProvider.when('/', {
            templateUrl : 'dashboard.html',
            controller: 'StroomMeterstandController'
        });
        $routeProvider.otherwise({redirectTo: 'dashboard.html'});
}]);

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
