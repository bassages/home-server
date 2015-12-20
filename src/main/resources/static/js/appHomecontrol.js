'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('appHomecontrol', [
    'ngRoute',
    'appHomecontrol.afvalController',
    'appHomecontrol.opgenomenVermogenService',
    'appHomecontrol.uurGrafiekController',
    'appHomecontrol.dagGrafiekController',
    'appHomecontrol.weekGrafiekController',
    'appHomecontrol.maandGrafiekController',
    'appHomecontrol.opgenomenVermogenController',
    'appHomecontrol.localizationService',
    'appHomecontrol.grafiekWindowSizeService'
]).
config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/grafiek/:type/uur', {
            templateUrl : 'grafiek.html',
            controller: 'UurGrafiekController'
        });
        $routeProvider.when('/grafiek/:type/dag', {
            templateUrl : 'grafiek.html',
            controller: 'DagGrafiekController'
        });
        $routeProvider.when('/grafiek/:type/week', {
            templateUrl : 'grafiek.html',
            controller: 'WeekGrafiekController'
        });
        $routeProvider.when('/grafiek/:type/maand', {
            templateUrl : 'grafiek.html',
            controller: 'MaandGrafiekController'
        });
        $routeProvider.when('/', {
            templateUrl : 'dashboard.html'
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
