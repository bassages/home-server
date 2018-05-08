(function() {
    'use strict';

    angular.module('templates', []);

    angular.module('app', [
        'ngRoute',
        'ngResource',
        'ngAnimate',
        'ui.bootstrap',
        'templates'
    ]);

    angular
        .module('app')
        .constant('DATETIME_CONSTANTS', {
                shortMonths : ["Jan.", "Feb.", "Maa.", "Apr.", "Mei.", "Jun.", "Jul.", "Aug.", "Sep.", "Okt.", "Nov.", "Dec."],
                shortDays : ["Zo.", "Ma.", "Di.", "Wo.", "Do.", "Vr.", "Za."],
                fullMonths : ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"]
            }
        );

    // Init Lodash
    angular.module('app')
        // allow DI for use in controllers, unit tests
        .constant('_', (<any>window)._);

    angular
        .module('app')
        .config(Config);

    Config.$inject = ['$routeProvider', '$compileProvider', '$httpProvider', 'DATETIME_CONSTANTS'];

    function Config($routeProvider, $compileProvider, $httpProvider, DATETIME_CONSTANTS) {
        // See https://docs.angularjs.org/guide/production
        $compileProvider.debugInfoEnabled(false);
        $compileProvider.commentDirectivesEnabled(false);
        $compileProvider.cssClassDirectivesEnabled(false);

        $httpProvider.defaults.withCredentials = true;
        $httpProvider.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';
        $httpProvider.defaults.xsrfCookieName = 'XSRF-TOKEN';

        numbro.registerLanguage({
            languageTag: "nl-NL",
            delimiters: {
                thousands: ".",
                decimal: ","
            },
            abbreviations: {
                thousand: "k",
                million: "mln",
                billion: "mrd",
                trillion: "bln"
            },
            ordinal: (number) => {
                let remainder = number % 100;
                return (number !== 0 && remainder <= 1 || remainder === 8 || remainder >= 20) ? "ste" : "de";
            },
            currency: {
                symbol: "€",
                position: "postfix",
                code: "EUR"
            },
            currencyFormat: {
                thousandSeparated: true,
                totalLength: 4,
                spaceSeparated: true,
                average: true
            },
            formats: {
                fourDigits: {
                    totalLength: 4,
                    spaceSeparated: true,
                    average: true
                },
                fullWithTwoDecimals: {
                    output: "currency",
                    mantissa: 2,
                    spaceSeparated: true,
                    thousandSeparated: true
                },
                fullWithTwoDecimalsNoCurrency: {
                    mantissa: 2,
                    thousandSeparated: true
                },
                fullWithNoDecimals: {
                    output: "currency",
                    spaceSeparated: true,
                    thousandSeparated: true,
                    mantissa: 0
                }
            }
        });
        numbro.setLanguage('nl-NL');

        let d3Formatters = d3.locale({
            "decimal": ",",
            "thousands": ".",
            "grouping": [3],
            "currency": ["€", ""],
            "dateTime": "%a %b %e %X %Y",
            "date": "%d-%m-%Y",
            "time": "%H:%M:%S",
            "periods": ["AM", "PM"],
            "days": ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
            "shortDays": DATETIME_CONSTANTS.shortDays,
            "months": DATETIME_CONSTANTS.fullMonths,
            "shortMonths": DATETIME_CONSTANTS.shortMonths
        });
        d3.time.format = d3Formatters.timeFormat;
        d3.format = d3Formatters.numberFormat;

        $routeProvider
            .when('/energie/stroom/opgenomen-vermogen', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'OpgenomenVermogenGrafiekController'
            })
            .when('/klimaat/historie/:sensortype', {
                templateUrl: 'app/klimaat/klimaat-historie.html',
                controller: 'KlimaatHistorieController',
                controllerAs: 'vm'
            })
            .when('/klimaat/top-charts/:sensortype', {
                templateUrl: 'app/klimaat/klimaat-top-charts.html',
                controller: 'KlimaatTopChartsController',
                controllerAs: 'vm'
            })
            .when('/klimaat/average/:sensortype', {
                templateUrl: 'app/klimaat/klimaat-average.html',
                controller: 'KlimaatAverageController',
                controllerAs: 'vm'
            })

            .when('/energie/:verbruiksoort/uur', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'UurEnergieHistorieController'
            })
            .when('/energie/:verbruiksoort/dag', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'DagEnergieHistorieController'
            })
            .when('/energie/:verbruiksoort/maand', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'MaandEnergieHistorieController'
            })
            .when('/energie/:verbruiksoort/jaar', {
                templateUrl: 'app/energie-historie/energie-historie.html',
                controller: 'JaarEnergieHistorieController'
            })

            .when('/energie/meterstanden', {
                templateUrl: 'app/meterstanden/meterstanden.html',
                controller: 'MeterstandenController',
                controllerAs: 'vm'
            })
            .when('/energiecontracten', {
                templateUrl: 'app/energiecontract/energiecontract.html',
                controller: 'EnergieContractController',
                controllerAs: 'vm'
            })
            .when('/mindergasnl', {
                templateUrl: 'app/mindergasnl/mindergasnl.html',
                controller: 'MindergasnlController',
                controllerAs: 'vm'
            })
            .when('/application-settings', {
                templateUrl: 'app/application-settings/application-settings.html',
                controller: 'ApplicationSettingsController',
                controllerAs: 'vm'
            })
            .when('/', {
                templateUrl: 'app/dashboard/dashboard.html',
                controller: 'DashboardController',
                controllerAs: 'vm'
            })
            .otherwise({redirectTo: 'index.html'});
    }
})();