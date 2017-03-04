(function() {
    'use strict';

    angular
        .module('app')
        .service('BaseHistorieService', BaseHistorieService);

    BaseHistorieService.$inject = ['ApplicationSettingsService'];

    function BaseHistorieService(ApplicationSettingsService) {
        var MINIMUM_HEIGHT = 220;
        var MAXIMUM_HEIGHT = 500;
        var AUTO_TABLE_CHART_THRESHOLD = 500;

        function setChartHeightMatchingWithAvailableWindowHeight(chart) {
            var rect = chart.element.getBoundingClientRect();
            var height = window.innerHeight - rect.top - 10;

            if (height < MINIMUM_HEIGHT) {
                height = MINIMUM_HEIGHT;
            } else if (height > MAXIMUM_HEIGHT) {
                height = MAXIMUM_HEIGHT;
            }
            chart.resize({height: height});
        }

        this.getStatisticsChartLines = function(statistics, valueFormatFunction) {
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

        this.setChartHeightMatchingWithAvailableWindowHeight = function(chart) {
            setChartHeightMatchingWithAvailableWindowHeight(chart);
        };

        function autoChartOrTable(theScope) {
            if (theScope.historicDataDisplayType === 'chart') {
                theScope.showChart = true;
                theScope.showTable = false;
            } else if (theScope.historicDataDisplayType === 'table') {
                theScope.showChart = false;
                theScope.showTable = true;
            } else if (ApplicationSettingsService.displayChartOrTable === 'chart') {
                theScope.showChart = true;
                theScope.showTable = false;
            } else if (ApplicationSettingsService.displayChartOrTable === 'table') {
                theScope.showChart = false;
                theScope.showTable = true;
            } else {
                theScope.showChart = window.innerWidth >= AUTO_TABLE_CHART_THRESHOLD;
                theScope.showTable = !theScope.showChart;
            }
        }

        this.manageChartSize = function(theScope) {
            autoChartOrTable(theScope);

            $(window).on("resize.doResize", function () {
                autoChartOrTable(theScope);
                theScope.$apply();

                if (typeof theScope.chart !== 'undefined') {
                    setChartHeightMatchingWithAvailableWindowHeight(theScope.chart);
                }
            });

            theScope.$on("$destroy", function () {
                $(window).off("resize.doResize"); //remove the handler added earlier
            });
        };
    }
})();
