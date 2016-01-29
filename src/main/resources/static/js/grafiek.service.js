(function() {
    'use strict';

    angular
        .module('app')
        .service('GrafiekService', GrafiekService);

    function GrafiekService() {
        var MINIMUM_HEIGHT = 220;
        var MAXIMUM_HEIGHT = 475;

        var soortData = 'verbruik'; // Default value

        this.getSoortData = function() {
            return soortData;
        };

        this.setSoortData = function(aSoortData) {
            soortData = aSoortData;
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
