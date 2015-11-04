'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('appHomecontrol', [
    'ngRoute',
    'appHomecontrol.afvalController',
    'appHomecontrol.opgenomenVermogenService',
    'appHomecontrol.uurGrafiekController',
    'appHomecontrol.dagGrafiekController',
    'appHomecontrol.opgenomenVermogenController'
]).
config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/grafiek/:type/uur', {
            templateUrl : 'uurGrafiek.html'
        });
        $routeProvider.when('/grafiek/:type/dag', {
            templateUrl : 'dagGrafiek.html'
        });
        $routeProvider.when('/', {
            templateUrl : 'dashboard.html'
        });
        $routeProvider.otherwise({redirectTo: 'dashboard.html'});
}]);

app.directive('formatteddate', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
            function fromUser(text) {
                return Date.parse(text);
            }
            function toUser(date) {
                return formatDate(date);
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});

// Returns a formatted date. Example: 21-10-2015
function formatDate(dateToFormat) {
    return pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}
