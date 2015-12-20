'use strict';

angular.module('appHomecontrol.weekGrafiekController', [])

    .controller('WeekGrafiekController', ['$scope', '$http', '$log', 'LocalizationService', 'GrafiekWindowSizeService', function($scope, $http, $log, LocalizationService, GrafiekWindowSizeService) {
        $scope.loading = false;
        $scope.chart = null;
        $scope.selection = new Date();
        $scope.period = 'WEEK';

        LocalizationService.localize();
        GrafiekWindowSizeService.manage($scope);

        $scope.isMaxSelected = function() {
            return (new Date()).getFullYear() == $scope.selection;
        };

        $scope.navigate = function(numberOfWeeks) {
            $scope.selection = new Date($scope.selection);
            $scope.selection.setFullYear($scope.selection.getFullYear() + numberOfWeeks);
            $scope.showGraph();
        };

        $scope.getDateFormat = function(text) {
            return 'yyyy';
        };

        function getTicksForEveryWeekInYear() {
            new Date($scope.selection).week();

            var tickValues = [];
            for (var i = 1; i <= 53; i++) {
                tickValues.push(i);
                $log.info('Tick: ' + i);
            }
            return tickValues;
        }

        $scope.showGraph = function() {
            $scope.loading = true;

            var graphDataUrl = 'rest/elektriciteit/verbruikPerWeekInJaar/' + $scope.selection.getFullYear();
            $log.info('URL: ' + graphDataUrl);

            var total = 0;
            var average = 0;

            $http({
                method: 'GET',
                url: graphDataUrl
            }).then(function successCallback(response) {
                var data = response.data;
                var tickValues = getTicksForEveryWeekInYear();

                var length = data.length;
                for (var i=0; i<length; i++) {
                    total += data[i].kWh;
                }
                average = total/length;

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.data = {};
                graphConfig.data.keys = {x: 'week', value: ['kWh', 'euro']};
                graphConfig.data.axes = {'euro': 'y2'};

                graphConfig.data.json = data;
                graphConfig.data.type = 'bar';
                graphConfig.data.types = {'euro': 'bar'};

                graphConfig.axis = {};
                graphConfig.axis.x = {tick: {format: '%U', values: tickValues, xcentered: true}, min: 0.5, max: 43.7, padding: {left: 0, right: 10}};
                graphConfig.axis.y = {label: {text: 'Verbruik', position: 'outer-middle'}, tick: {format: function (d) { return d + ' kWh'; }}};
                graphConfig.axis.y2 = {label: {text: 'Kosten', position: 'outer-middle'}, show: true, tick: {format: d3.format('$.2f')}};
                graphConfig.legend = {show: false};
                graphConfig.bar = {width: {ratio: 0.8}};
                graphConfig.point = {show: false};
                graphConfig.transition = {duration: 0};
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
