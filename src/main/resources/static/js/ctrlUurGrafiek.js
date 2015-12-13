'use strict';

angular.module('appHomecontrol.uurGrafiekController', [])

    .controller('UurGrafiekController', ['$scope', '$http', '$log', 'GrafiekWindowSizeService', function($scope, $http, $log, GrafiekWindowSizeService) {
        $scope.loading = false;
        $scope.period = 'HOUR';

        // By default, today is selected
        $scope.selection = new Date();
        $scope.selection.setHours(0,0,0,0);

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
            var next = new Date($scope.selection);
            next.setDate($scope.selection.getDate() + numberOfDays);
            $scope.selection = next;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.selection);
            $scope.showGraph();
        };

        function getTicksForEveryHourInPeriod(from, to) {
            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;
            $log.info('numberOfHoursInDay: ' + numberOfHoursInDay);

            // Add one tick for every hour
            var tickValues = [];
            for (var i = 0; i <= numberOfHoursInDay; i++) {
                var tickValue = from.getTime() + (i * 60 * 60 * 1000);
                tickValues.push(tickValue);
                $log.debug('Add tick for ' + new Date(tickValue));
            }
            return tickValues;
        }

        $scope.showGraph = function() {
            $scope.loading = true;

            var subPeriodLength = 6 * 60 * 1000;

            var to = new Date($scope.selection);
            to.setDate($scope.selection.getDate() + 1);

            var graphDataUrl = 'rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.selection.getTime() + '/' + to.getTime() + '?subPeriodLength=' + subPeriodLength;
            $log.info('URL: ' + graphDataUrl);

            var total = 0;
            var average = 0;

            $http({
                method: 'GET',
                url: graphDataUrl
            }).then(function successCallback(response) {
                var data = response.data;
                var tickValues = getTicksForEveryHourInPeriod($scope.selection ,to);

                var length = data.length;
                for (var i=0; i<length; i++) {
                    var subPeriodEnd = data[i].dt + (subPeriodLength - 1);
                    data.push({dt: subPeriodEnd, watt: data[i].watt});
                    total += data[i].watt;
                }
                average = total/length;

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.data = {};
                graphConfig.data.keys = {x: "dt", value: ["watt"]};
                graphConfig.data.json = data;
                graphConfig.data.types= {"watt": "area"};
                graphConfig.axis = {};
                graphConfig.axis.x = {type: "timeseries", tick: {format: "%H:%M", values: tickValues, rotate: -90}, min: $scope.selection, max: to, padding: {left: 0, right:10}};
                graphConfig.axis.y = {label: {text: "Opgenomen vermogen in watt", position: "outer-middle"}};
                graphConfig.legend = {show: false};
                graphConfig.bar = {width: {ratio: 1}};
                graphConfig.point = {show: false};
                graphConfig.transition = {duration: 0};
                graphConfig.grid = {y: {show: true}};
                graphConfig.tooltip = {show: false};
                graphConfig.padding = {top: 0, right: 5, bottom: 40, left: 50};
                if (average > 0) {
                    graphConfig.grid.y.lines = [{value: average, text: '', class: 'gemiddelde'}];
                }

                $scope.chart = c3.generate(graphConfig);

                GrafiekWindowSizeService.manage($scope);
                $scope.loading = false;

            }, function errorCallback(response) {
                $scope.loading = false;
            });
        }
    }]);

