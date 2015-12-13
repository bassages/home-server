'use strict';

// TODO: https://github.com/eternicode/bootstrap-datepicker/issues/615

angular.module('appHomecontrol.dagGrafiekController', [])

    .controller('DagGrafiekController', ['$scope', '$http', '$log', 'D3LocalizationService', 'GrafiekWindowSizeService', function($scope, $http, $log, D3LocalizationService, GrafiekWindowSizeService) {
        var oneDay = 24 * 60 * 60 * 1000;
        var halfDay = 12 * 60 * 60 * 1000;
        $scope.period = 'DAY';
        $scope.loading = false;

        // By default, today is the last day in the graph
        $scope.selection = new Date();
        $scope.selection.setHours(0,0,0,0);
        $scope.from = new Date($scope.selection);
        $scope.from.setDate($scope.from.getDate() - 6);

        D3LocalizationService.localize();

        var numberOfDaysInPeriod = (($scope.selection.getTime() - $scope.from.getTime()) / oneDay) + 1;
        $log.debug('Period: ' + $scope.from + ' - ' + $scope.selection);
        $log.debug('numberOfDaysInPeriod: ' + numberOfDaysInPeriod);

        var applyDatePickerUpdatesInAngularScope = false;
        var theDatepicker = $('.datepicker');
        theDatepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            format: "dd-mm-yyyy"
        });
        theDatepicker.on('changeDate', function(e) {
            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    var from = new Date($scope.selection);
                    from.setDate(from.getDate() - 6);
                    $scope.from = from;
                    $log.debug("changeDate() " + $scope.from + ' - ' + $scope.selection);
                    $scope.showGraph();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });
        theDatepicker.datepicker('setDate', $scope.selection);

        $scope.getDateFormat = function(text) {
            return 'dd-MM-yyyy';
        };

        $scope.isMaxSelected = function() {
            var result = false;

            var today = new Date();
            today.setHours(0,0,0,0);

            if ($scope.selection) {
                result = today.getTime() == $scope.selection.getTime();
            }
            return result;
        };

        $scope.navigate = function(numberOfDays) {
            var nextFrom = new Date($scope.from);
            nextFrom.setDate($scope.from.getDate() + numberOfDays);
            $scope.from = nextFrom;

            var nextTo = new Date($scope.selection);
            nextTo.setDate($scope.selection.getDate() + numberOfDays);
            $scope.selection = nextTo;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.from);
            $scope.showGraph();
        };

        function getTicksForEveryDayInPeriod() {
            // Add one tick for every day
            var tickValues = [];
            for (var i = 0; i < numberOfDaysInPeriod; i++) {
                var tickValue = $scope.from.getTime() + (i * oneDay);
                tickValues.push(tickValue);
                $log.debug('Add tick for ' + new Date(tickValue));
            }
            return tickValues;
        }

        $scope.showGraph = function() {
            $scope.loading = true;

            var graphDataUrl = 'rest/elektriciteit/verbruikPerDag/' + $scope.from.getTime() + '/' + $scope.selection.getTime();
            $log.info('URL: ' + graphDataUrl);

            var total = 0;
            var average = 0;

            $http({
                method: 'GET',
                url: graphDataUrl
            }).then(function successCallback(response) {
                var data = response.data;
                var tickValues = getTicksForEveryDayInPeriod();

                var length = data.length;
                for (var i=0; i<length; i++) {
                    total += data[i].kWh;
                }
                average = total/length;

                var xMin = new Date($scope.from.getTime()) - halfDay;
                var xMax = new Date($scope.selection.getTime() + halfDay);

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.data = {};
                graphConfig.data.keys = {x: 'dt', value: ['kWh', 'euro']};
                graphConfig.data.axes = {'euro': 'y2'};

                graphConfig.data.json = data;
                graphConfig.data.type = 'bar';
                graphConfig.data.types = {'euro': 'bar'};

                graphConfig.axis = {};
                graphConfig.axis.x = {type: "timeseries", tick: {format: "%a %d-%m", values: tickValues, centered: true, multiline: true, width: 35}, min: xMin, max: xMax, padding: {left: 0, right:10}};
                graphConfig.axis.y = {label: {text: "Verbruik", position: "outer-middle"}, tick: {format: function (d) { return d + ' kWh'; }}};
                graphConfig.axis.y2 = {label: {text: 'Kosten', position: "outer-middle"}, show: true, tick: {format: d3.format("$.2f")}};
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

                GrafiekWindowSizeService.manage($scope);
                $scope.loading = false;

            }, function errorCallback(response) {
                $scope.loading = false;
            });
        }
    }]);

