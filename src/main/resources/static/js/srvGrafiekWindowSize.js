'use strict';

angular.module('appHomecontrol.grafiekWindowSizeService', [])

    .service('GrafiekWindowSizeService', function() {

        function setGraphHeightMatchingWithAvailableWindowHeight(chart) {
            chart.resize({height: (window.innerHeight - 70)});
        }

        this.manage = function(theScope) {

            $(window).on("resize.doResize", function () {
                setGraphHeightMatchingWithAvailableWindowHeight(theScope.chart);
            });
            theScope.$on("$destroy",function () {
                $(window).off("resize.doResize"); //remove the handler added earlier
            });
            // Set initial size
            setGraphHeightMatchingWithAvailableWindowHeight(theScope.chart);
        };
    });
