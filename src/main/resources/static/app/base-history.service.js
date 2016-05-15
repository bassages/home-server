(function() {
    'use strict';

    angular
        .module('app')
        .service('BaseHistorieService', BaseHistorieService);

    function BaseHistorieService() {
        var MINIMUM_HEIGHT = 220;
        var MAXIMUM_HEIGHT = 500;

        function setGraphHeightMatchingWithAvailableWindowHeight(chart) {
            var rect = chart.element.getBoundingClientRect();
            var height = window.innerHeight - rect.top - 10;

            if (height < MINIMUM_HEIGHT) {
                height = MINIMUM_HEIGHT;
            } else if (height > MAXIMUM_HEIGHT) {
                height = MAXIMUM_HEIGHT;
            }
            chart.resize({height: height});
        }

        this.getStatisticsGraphLines = function(statistics, valueFormatFunction) {
            var lines = [];
            if (statistics.avg) {
                lines.push({
                    value: statistics.avg, class: 'avg', position: 'middle',
                    text: 'Gemiddelde: ' + valueFormatFunction(statistics.avg)
                });
            }
            if (statistics.min) {
                lines.push({
                    value: statistics.min, class: 'min', position: 'start',
                    text: 'Laagste: ' + valueFormatFunction(statistics.min)
                });
            }
            if (statistics.max) {
                lines.push({
                    value: statistics.max, class: 'max',
                    text: 'Hoogste: ' + valueFormatFunction(statistics.max)
                });
            }
            return lines;
        };

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
