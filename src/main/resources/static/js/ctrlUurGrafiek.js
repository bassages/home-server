(function() {
    'use strict';

    angular
        .module('app')
        .controller('UurGrafiekController', ['$scope', '$routeParams', '$http', '$log', 'LoadingIndicatorService', 'SharedDataService', 'LocalizationService', 'GrafiekWindowSizeService', UurGrafiekController]);

    function UurGrafiekController($scope, $routeParams, $http, $log, LoadingIndicatorService, SharedDataService, LocalizationService, GrafiekWindowSizeService) {
        initialize();

        function initialize() {
            $scope.period = 'uur';
            $scope.energiesoort = $routeParams.energiesoort;
            $scope.periode = $routeParams.periode;
            $scope.soort = 'verbruik'; // This controller only supports verbruik
            SharedDataService.setSoortData('verbruik');
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'Watt'}];
            // By default, today is selected
            $scope.selection = new Date();
            $scope.selection.setHours(0, 0, 0, 0);
            GrafiekWindowSizeService.manage($scope);
            LocalizationService.localize();
            Date.CultureInfo.abbreviatedDayNames = LocalizationService.getShortDays();

            clearGraph();
            getDataFromServer();
        }

        var applyDatePickerUpdatesInAngularScope = false;
        var theDatepicker = $('.datepicker');
        theDatepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            daysOfWeekHighlighted: "0,6",
            format: {
                toDisplay: function (date, format, language) {
                    var formatter = d3.time.format('%a %d-%m-%Y');
                    return formatter(date);
                },
                toValue: function (date, format, language) {
                    if (date == '0d') {
                        return new Date();
                    }
                    return d3.time.format('%a %d-%m-%Y').parse(date);
                }
            }
        });
        theDatepicker.on('changeDate', function(e) {
            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    getDataFromServer();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });
        theDatepicker.datepicker('setDate', $scope.selection);

        $scope.getDateFormat = function(text) {
            return 'ddd dd-MM-yyyy';
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

        $scope.showNumberOfPeriodsSelector = function() {
            return false;
        };

        $scope.navigate = function(numberOfPeriods) {
            var next = new Date($scope.selection);
            next.setDate($scope.selection.getDate() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.selection);

            $scope.selection = next;

            getDataFromServer();
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

        function getAverage(data) {
            var total = 0;
            var length = data.length;
            for (var i = 0; i < length; i++) {
                var subPeriodEnd = data[i].dt + (getSubPeriodLength() - 1);
                data.push({dt: subPeriodEnd, watt: data[i].watt});
                total += data[i].watt;
            }
            return total / length;
        }

        function getSubPeriodLength() {
            return 6 * 60 * 1000;
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

            var average = getAverage(graphData);
            if (average > 0) {
                graphConfig.grid.y.lines = [{value: average, text: '', class: 'gemiddelde'}];
            }
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
            GrafiekWindowSizeService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo() {
            var to = new Date($scope.selection);
            to.setDate($scope.selection.getDate() + 1);
            return to;
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var graphDataUrl = 'rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.selection.getTime() + '/' + getTo().getTime() + '?subPeriodLength=' + getSubPeriodLength();
            $log.info('URL: ' + graphDataUrl);

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
