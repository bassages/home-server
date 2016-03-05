(function() {
    'use strict';

    angular
        .module('app')
        .service('GrafiekService', GrafiekService);

    function GrafiekService() {
        var MINIMUM_HEIGHT = 220;
        var MAXIMUM_HEIGHT = 475;

        numbro.culture('nl-NL');

        this.getVerbruikLabel = function(energiesoort) {
            if (energiesoort == 'stroom') {
                return 'kWh'
            } else if (energiesoort == 'gas') {
                return 'M\u00B3';
            } else {
                return '???';
            }
        };

        this.formatWithoutUnitLabel = function(soortData, value) {
            if (soortData == 'verbruik') {
                return numbro(value).format('0.000');
            } else if (soortData == 'kosten') {
                return numbro(value).format('0.00');
            }
        };

        this.formatWithUnitLabel = function(soortData, energieSoorten, value) {
            var withoutUnitLabel = this.formatWithoutUnitLabel(soortData, value);
            if (soortData == 'verbruik') {
                return withoutUnitLabel + ' ' + this.getVerbruikLabel(energieSoorten[0]);
            } else if (soortData == 'kosten') {
                return '\u20AC ' + withoutUnitLabel;
            }
        };

        function setGraphHeightMatchingWithAvailableWindowHeight(chart) {
            var height = window.innerHeight - 115;

            if (height < MINIMUM_HEIGHT) {
                height = MINIMUM_HEIGHT;
            } else if (height > MAXIMUM_HEIGHT) {
                height = MAXIMUM_HEIGHT;
            }
            chart.resize({height: height});
        }

        this.setGraphHeightMatchingWithAvailableWindowHeight = function(chart) {
            setGraphHeightMatchingWithAvailableWindowHeight(chart);
        };

        this.manageGraphSize = function(theScope) {
            $(window).on("resize.doResize", function () {
                if (typeof theScope.chart !== 'undefined') {
                    setGraphHeightMatchingWithAvailableWindowHeight(theScope.chart);
                }
            });
            theScope.$on("$destroy", function () {
                $(window).off("resize.doResize"); //remove the handler added earlier
            });
        };
    }
})();
