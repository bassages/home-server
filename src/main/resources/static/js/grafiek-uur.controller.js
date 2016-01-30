(function() {
    'use strict';

    angular
        .module('app')
        .controller('UurGrafiekController', UurGrafiekController);

    UurGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function UurGrafiekController($scope, $routeParams, $http, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        var SIX_MINUTES_IN_MILLISECONDS = 6 * 60 * 1000;

        activate();

        function activate() {
            var today = new Date();
            today.setHours(0,0,0,0);
            $scope.selection = today;

            $scope.energiesoort = $routeParams.energiesoort;
            $scope.period = 'uur';
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'Watt'}];
            $scope.soort = 'verbruik'; // This controller only supports verbruik

            GrafiekService.setSoortData($scope.soort);
            GrafiekService.manageGraphSize($scope);

            LocalizationService.localize();

            clearGraph();
            getDataFromServer();
        }

        $scope.getD3DateFormat = function() {
            return '%a %d-%m-%Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            daysOfWeekHighlighted: "0,6",
            format: {
                toDisplay: function (date, format, language) {
                    var formatter = d3.time.format($scope.getD3DateFormat());
                    return formatter(date);
                },
                toValue: function (date, format, language) {
                    if (date == '0d') {
                        return new Date();
                    }
                    return d3.time.format($scope.getD3DateFormat()).parse(date);
                }
            }
        });

        datepicker.datepicker('setDate', $scope.selection);
        var applyDatePickerUpdatesInAngularScope = true;

        datepicker.on('changeDate', function(e) {
            if (applyDatePickerUpdatesInAngularScope) {
                $log.info("changeDate event from datepicker. Selected date: " + e.date);

                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    getDataFromServer();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });

        $scope.isMaxSelected = function() {
            var result = false;

            var today = new Date();
            today.setHours(0,0,0,0);

            if ($scope.selection) {
                result = today.getTime() == $scope.selection.getTime();
            }
            return result;
        };

        $scope.showNumberOfPeriodsSelector = function() {
            return false;
        };

        $scope.navigate = function(numberOfPeriods) {
            var next = new Date($scope.selection);
            next.setDate($scope.selection.getDate() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            datepicker.datepicker('setDate', next);

            $scope.selection = next;

            getDataFromServer();
        };

        function getTicksForEveryHourInPeriod(from, to) {
            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;

            // Add one tick for every hour
            var tickValues = [];
            for (var i = 0; i <= numberOfHoursInDay; i++) {
                var tickValue = from.getTime() + (i * 60 * 60 * 1000);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function addSubPeriodEnd(data) {
            var length = data.length;
            for (var i = 0; i < length; i++) {
                var subPeriodEnd = data[i].dt + (SIX_MINUTES_IN_MILLISECONDS - 1);
                data.push({dt: subPeriodEnd, watt: data[i].watt});
            }
        }

        function getStatistics(graphData) {
            var min;
            var max;
            var avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < graphData.length; i++) {
                var data = graphData[i].watt;

                if (data != null && (typeof max=='undefined' || data > max)) {
                    max = data;
                }
                if (data != null && data > 0 && (typeof min=='undefined' || data < min)) {
                    min = data;
                }
                if (data != null) {
                    total += data;
                    nrofdata += 1;
                }
            }

            if (nrofdata > 0) {
                avg = total / nrofdata;
            }
            return {avg: avg, min: min, max: max};
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 20, left: 50, right: 20}
            }
        }

        function getGraphConfig(graphData) {
            var graphConfig = {};
            var tickValues = getTicksForEveryHourInPeriod($scope.selection, getTo());

            graphConfig.bindto = '#chart';
            graphConfig.data = {json: graphData, keys: {x: "dt", value: ["watt"]}, types: {"watt": "area"}};
            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -90},
                min: $scope.selection,
                max: getTo(),
                padding: {left: 0, right: 10}
            };
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 1}};
            graphConfig.point = {show: false};
            graphConfig.transition = {duration: 0};
            graphConfig.tooltip = {show: false};
            graphConfig.padding = {top: 10, bottom: 45, left: 50, right: 20};
            graphConfig.grid = {y: {show: true}};

            var statistics = getStatistics(graphData);

            var lines = [];
            if (statistics.avg) {
                lines.push({value: statistics.avg, text: 'Gemiddelde: ' + Math.round(statistics.avg), class: 'avg', position: 'middle'});
            }
            if (statistics.min) {
                lines.push({value: statistics.min, text: 'Laagste: ' + statistics.min, class: 'min', position: 'start'});
            }
            if (statistics.max) {
                lines.push({value: statistics.max, text: 'Hoogste: ' + statistics.max, class: 'max'});
            }
            graphConfig.grid.y.lines = lines;

            addSubPeriodEnd(graphData);

            return graphConfig;
        }

        function clearGraph() {
            loadDataIntoGraph([]);
        }

        function loadDataIntoGraph(graphData) {
            $scope.graphData = graphData;

            var graphConfig;
            if (graphData.length == 0) {
                graphConfig = getEmptyGraphConfig();
            } else {
                graphConfig = getGraphConfig(graphData);
            }
            $scope.chart = c3.generate(graphConfig);
            GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo() {
            var to = new Date($scope.selection);
            to.setDate($scope.selection.getDate() + 1);
            return to;
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var graphDataUrl = 'rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.selection.getTime() + '/' + getTo().getTime() + '?subPeriodLength=' + SIX_MINUTES_IN_MILLISECONDS;
            $log.info('Getting data for graph from URL: ' + graphDataUrl);

            $http({
                method: 'GET', url: graphDataUrl
            }).then(function successCallback(response) {
                loadDataIntoGraph(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
            });
        }
    }
})();

