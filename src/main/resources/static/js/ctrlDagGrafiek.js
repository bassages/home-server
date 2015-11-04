'use strict';

angular.module('appHomecontrol.dagGrafiekController', [])

    .controller('DagGrafiekController', ['$scope', '$http', '$log', '$routeParams', function($scope, $http, $log, $routeParams) {
        $scope.chart = null;
        $scope.config= {};

        $log.debug($routeParams.type, $routeParams.periode);

        // By default, today is selected
        $scope.from = new Date();
        $scope.from.setHours(0,0,0,0);
        $scope.to = new Date($scope.from);
        $scope.to.setDate($scope.from.getDate() + 1);

        var applyDatePickerUpdatesInAngularScope = false;
        var theDatepicker = $('.datepicker');
        theDatepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d"
        });
        theDatepicker.on('changeDate', function(e) {
            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.from = new Date(e.date);
                    $scope.showGraph();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });
        theDatepicker.datepicker('setDate', $scope.from);

        $scope.isTodaySelected = function() {
            var result = false;

            var today = new Date();
            today.setHours(0,0,0,0);

            if ($scope.from) {
                result = today.getTime() == $scope.from.getTime();
            }
            return result;
        };

        $scope.previousDay = function() {
            var previous = new Date($scope.from);
            previous.setDate($scope.from.getDate() - 1);
            $scope.from = previous;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.from);
            $scope.showGraph();
        };

        $scope.nextDay = function() {
            var next = new Date($scope.from);
            next.setDate($scope.from.getDate() + 1);
            $scope.from = next;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.from);
            $scope.showGraph();
        };

        $scope.dateChanged = function() {
            $scope.showGraph();
        };

        function getTicksForEveryDayInPeriod() {
            var numberOfDaysInPeriod = (($scope.to - $scope.from) / 1000) / 60 / 60 / 24;
            $log.info('numberOfDaysInPeriod: ' + numberOfDaysInPeriod);

            // Add one tick for every day
            var tickValues = [];
            for (var i = 0; i <= numberOfDaysInPeriod; i++) {
                var tickValue = $scope.from.getTime() + (i * 24 * 60 * 60 * 1000);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function setDataColor() {
            // Dirty fix to set opacity...
            $('.c3-area-watt').attr('style', 'fill: rgb(31, 119, 180); opacity: 0.8;');
        }

        // TODO: https://github.com/mbostock/d3/wiki/Localization#locale_timeFormat

        $scope.showGraph = function() {

            $scope.to = new Date($scope.from);
            $scope.to.setDate($scope.from.getDate() + 7);

            //var graphDataUrl = 'rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.from.getTime() + '/' + $scope.to.getTime() + '?subPeriodLength=' + subPeriodLength;
            //$log.info('URL: ' + graphDataUrl);

            //$http.get(graphDataUrl).success(function(data) {
                //if (data) {
                //    var length = data.length;
                //    for (var i=0; i<length; i++) {
                //        var subPeriodEnd = data[i].dt + (subPeriodLength - 1);
                //        data.push({dt: subPeriodEnd, watt: data[i].watt});
                //    }
                //}

                var tickValues = getTicksForEveryDayInPeriod();

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.onresized = setDataColor;
                graphConfig.data = {};
                graphConfig.data.keys = {x: "dt", value: ["kWh"]};
                graphConfig.data.json = [{dt: 1446591600000, kWh: 19}];
                graphConfig.data.types= {"kWh": "bar"};
                graphConfig.data.empty = {label: {text: "Gegevens worden opgehaald..."}};

                graphConfig.axis = {};
                graphConfig.axis.x = {type: "timeseries", tick: {format: "%a", values: tickValues, centered: true}, min: $scope.from, max: $scope.to, padding: {left: 0, right:10}};
                graphConfig.axis.y = {label: {text: "kWh", position: "outer-middle"}};

                graphConfig.legend = {show: false};
                graphConfig.bar = {width: {ratio: 1}};
                graphConfig.point = { show: false};
                graphConfig.transition = { duration: 0};
                graphConfig.grid = {y: {show: true}};
                graphConfig.tooltip = {show: false};
                graphConfig.padding = {top: 0, right: 5, bottom: 40, left: 50};

                $scope.chart = c3.generate(graphConfig);

                setDataColor();
            //});
        }
    }]);

