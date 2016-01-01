'use strict';

angular.module('appHomecontrol.maandGrafiekController', [])

    .controller('MaandGrafiekController', ['$scope', '$http', '$log', 'LocalizationService', 'GrafiekWindowSizeService', function($scope, $http, $log, LocalizationService, GrafiekWindowSizeService) {
        $scope.loading = false;
        $scope.chart = null;
        $scope.selection = new Date();
        $scope.period = 'MONTH';

        LocalizationService.localize();
        GrafiekWindowSizeService.manage($scope);

        $scope.isMaxSelected = function() {
            return (new Date()).getFullYear() == $scope.selection;
        };

        $scope.showNumberOfPeriodsSelector = function() {
            return false;
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = new Date($scope.selection);
            $scope.selection.setFullYear($scope.selection.getFullYear() + numberOfPeriods);
            $scope.showGraph();
        };

        $scope.getDateFormat = function(text) {
            return 'yyyy';
        };

        function getTicksForEveryMonthInYear() {
            var tickValues = [];
            for (var i = 1; i <= 12; i++) {
                tickValues.push(i);
                $log.info('Tick: ' + i);
            }
            return tickValues;
        }

        $scope.showGraph = function() {
            $scope.loading = true;

            var graphDataUrl = 'rest/elektriciteit/verbruikPerMaandInJaar/' + $scope.selection.getFullYear();
            $log.info('URL: ' + graphDataUrl);

            var total = 0;
            var average = 0;

            $http({
                method: 'GET',
                url: graphDataUrl
            }).then(function successCallback(response) {
                var data = response.data;
                var tickValues = getTicksForEveryMonthInYear();

                var length = data.length;
                for (var i=0; i<length; i++) {
                    total += data[i].kWh;
                }
                average = total/length;

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.data = {};
                //graphConfig.data.keys = {x: 'maand', value: ['kWh', 'euro']};
                graphConfig.data.keys = {x: 'maand', value: ['kWh']};
                //graphConfig.data.axes = {'euro': 'y2'};

                graphConfig.data.json = data;
                graphConfig.data.type = 'bar';
                //graphConfig.data.types = {'euro': 'bar'};

                graphConfig.axis = {};
                graphConfig.axis.x = {tick: {format: function (d) { return LocalizationService.getShortMonths()[d-1]; }, values: tickValues, xcentered: true}, min: 0.5, max: 2.5, padding: {left: 0, right:10}};
                graphConfig.axis.y = {label: {text: "Verbruik", position: "outer-middle"}, tick: {format: function (d) { return d + ' kWh'; }}};
                //graphConfig.axis.y2 = {label: {text: 'Kosten', position: "outer-middle"}, show: true, tick: {format: d3.format("$.2f")}};
                graphConfig.legend = {show: false};
                graphConfig.bar = {width: {ratio: 0.8}};
                graphConfig.point = { show: false};
                graphConfig.transition = { duration: 0};
                graphConfig.grid = {y: {show: true}};
                graphConfig.tooltip = {show: false};
                graphConfig.padding = {top: 0, right: 70, bottom: 40, left: 70};
                graphConfig.interaction= {enabled: false};
                if (average > 0) {
                    graphConfig.grid.y.lines = [{value: average, text: '', class: 'gemiddelde'}];
                }
                $scope.chart = c3.generate(graphConfig);
                $scope.chart.hide(['euro']);

                GrafiekWindowSizeService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
                $scope.loading = false;

            }, function errorCallback(response) {
                $scope.loading = false;
            });
        }
    }]);
