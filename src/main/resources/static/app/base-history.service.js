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
            return _.filter( [
                                createStatisticChartLine(statistics.avg, 'avg', 'middle', 'Gemiddelde: ', valueFormatFunction),
                                createStatisticChartLine(statistics.min, 'min', 'start', 'Laagste: ', valueFormatFunction),
                                createStatisticChartLine(statistics.max, 'max', 'end', 'Hoogste: ', valueFormatFunction)
                             ], _.isObject);
        };

        function createStatisticChartLine(value, clazz, position, textPrefix, valueFormatFunction) {
            if (value) {
                return { value: value, class: clazz, position: position, text: textPrefix + valueFormatFunction(value) };
            }
        }

        this.setChartHeightMatchingWithAvailableWindowHeight = function(chart) {
            setChartHeightMatchingWithAvailableWindowHeight(chart);
        };

        function showChartOrTableCallback(showChartCallBackFunction, showTableCallBackFunction) {
            if (ApplicationSettingsService.displayChartOrTable === 'chart') {
                showChartCallBackFunction();
            } else if (ApplicationSettingsService.displayChartOrTable === 'table') {
                showTableCallBackFunction();
            } else {
                if (window.innerWidth >= AUTO_TABLE_CHART_THRESHOLD) {
                    showChartCallBackFunction();
                } else {
                    showTableCallBackFunction();
                }
            }
        }

        this.manageChartSize = function(theScope, showChartCallBackFunction, showTableCallBackFunction) {

            showChartOrTableCallback(showChartCallBackFunction, showTableCallBackFunction);

            $(window).on("resize.doResize", function () {
                showChartOrTableCallback(showChartCallBackFunction, showTableCallBackFunction);
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
