'use strict';

// Declare app level module which depends on views, and components
var app = angular.module('appHomecontrol', [
    'ngRoute',
    'ngResource',

    'ui.bootstrap',

    'appHomecontrol.loadingIndicatorService',

    'appHomecontrol.kostenService',
    'appHomecontrol.kostenController',

    'appHomecontrol.meterstandService',
    'appHomecontrol.grafiekWindowSizeService',
    'appHomecontrol.uurGrafiekController',
    'appHomecontrol.dagGrafiekController',
    'appHomecontrol.maandGrafiekController',

    'appHomecontrol.stroomMeterstandController',

    'appHomecontrol.localizationService',
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
                if(date) {
                    return date.toString(scope.getDateFormat());
                } else {
                    return '';
                }
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});

app.directive('formattedepochtimestamp', function() {
    return {
        restrict: 'A', // only matches attribute
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
            function fromUser(text) {
                return Date.parse(text).getTime();
            }
            function toUser(epochDate) {
                if(epochDate) {
                    return (new Date(epochDate)).toString('dd-MM-yyyy');
                } else {
                    return null;
                }
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});

app.directive('energieprijs', function() {
    return {
        restrict: 'A', // only matches attribute
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
            function fromUser(text) {
                var validate = text.replace(',','.');
                if (!isNaN(parseFloat(validate)) && isFinite(validate)) {
                    ngModel.$setValidity("energieprijs", true);
                    return parseFloat(validate);
                } else {
                    ngModel.$setValidity("energieprijs", false);
                    return null;
                }
            }
            function toUser(prijs) {
                if(prijs != null) {
                    ngModel.$setValidity("energieprijs", true);
                    return prijs.toFixed(4).replace('.',',');
                } else {
                    return null;
                }
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});

app.directive('epochmodeldatepicker', function() {
    return {
        restrict: 'A',
        require : 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            function fromUser(text) {
                var date = d3.time.format('%d-%m-%Y').parse(text);
                if (date) {
                    return date.getTime();
                } else {
                    return null;
                }
            }
            function toUser(epochTimestamp) {
                if(epochTimestamp) {
                    var date = new Date(epochTimestamp);
                    element.datepicker('setDate', date);
                    return date.toString('dd-MM-yyyy');
                } else {
                    return null;
                }
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);

            element.datepicker({
                autoclose: true,
                todayBtn: "linked",
                calendarWeeks: true,
                todayHighlight: true,
                language:"nl",
                daysOfWeekHighlighted: "0,6"
            });
        }
    };
});
