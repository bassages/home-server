(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagGrafiekController', ['$scope', '$routeParams', '$http', '$log', 'LoadingIndicatorService', 'SharedDataService', 'LocalizationService', 'GrafiekWindowSizeService', DagGrafiekController]);

    function DagGrafiekController($scope, $routeParams, $http, $log, LoadingIndicatorService, SharedDataService, LocalizationService, GrafiekWindowSizeService) {
        var oneDay = 24 * 60 * 60 * 1000;
        var halfDay = 12 * 60 * 60 * 1000;

        $scope.initialize = function() {
            $scope.loading = false;
            $scope.period = 'dag';
            $scope.energiesoort = $routeParams.energiesoort;
            $scope.periode = $routeParams.periode;
            $scope.soort = SharedDataService.getSoortData();
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'kWh'}, {
                'code': 'kosten',
                'omschrijving': '\u20AC'
            }];
            // By default, today is the last day in the graph
            $scope.selection = new Date();
            $scope.selection.setHours(0, 0, 0, 0);
            $scope.numberOfPeriods = 7;
            Date.CultureInfo.abbreviatedDayNames = LocalizationService.getShortDays();
            LocalizationService.localize();
            GrafiekWindowSizeService.manage($scope);

            clearGraph();
            getDataFromServer();
        };

        $scope.initialize();

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

        $scope.navigate = function(numberOfPeriods) {
            var selection = new Date($scope.selection);
            selection.setDate($scope.selection.getDate() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', selection);

            $scope.selection = selection;
            getDataFromServer();
        };

        $scope.switchSoort = function(destinationSoortCode) {
            $scope.soort = destinationSoortCode;
            SharedDataService.setSoortData(destinationSoortCode);
            loadDataIntoGraph($scope.graphData);
        };

        $scope.showNumberOfPeriodsSelector = function() {
            return true;
        };

        $scope.setNumberOfPeriods = function(numberOfPeriods) {
            if (($scope.numberOfPeriods + numberOfPeriods) >= 1) {
                $scope.numberOfPeriods = $scope.numberOfPeriods + numberOfPeriods;
                getDataFromServer();
            }
        };

        function getTicksForEveryDayInPeriod() {
            var tickValues = [];
            for (var i = 0; i <= ($scope.numberOfPeriods-1); i++) {
                var tickValue = $scope.selection.getTime() - (i * oneDay);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function getAverage(graphData) {
            var total = 0;
            var length = graphData.length;
            for (var i = 0; i < length; i++) {
                if ($scope.soort == 'verbruik') {
                    total += graphData[i].kWh;
                } else if ($scope.soort == 'kosten') {
                    total += graphData[i].euro;
                }
            }
            return total / length;
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

            var tickValues = getTicksForEveryDayInPeriod();

            var xMin = new Date(getFrom().getTime()) - halfDay;
            var xMax = new Date($scope.selection.getTime() + halfDay);

            graphConfig.bindto = '#chart';

            var value;
            if ($scope.soort == 'verbruik') {
                value = 'kWh';
            } else if ($scope.soort == 'kosten') {
                value = 'euro';
            }
            graphConfig.data = {};
            graphConfig.data.json = graphData;
            graphConfig.data.type = 'bar';
            graphConfig.data.keys = {x: 'dt', value: [value]};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: 'timeseries',
                tick: {format: "%a %d-%m", values: tickValues, centered: true, multiline: true, width: 35},
                min: xMin,
                max: xMax,
                padding: {left: 0, right: 10}
            };

            if ($scope.soort == 'kosten') {
                graphConfig.axis.y = {tick: {format: d3.format(".2f")}};
            }

            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                format: {
                    name: function (name, ratio, id, index) {
                        return $scope.soort.charAt(0).toUpperCase() + $scope.soort.slice(1);
                    },
                    value: function (value, ratio, id) {
                        if ($scope.soort == 'verbruik') {
                            return value + ' kWh';
                        } else if ($scope.soort == 'kosten') {
                            var format = d3.format(".2f");
                            return '\u20AC ' + format(value);
                        }
                    }
                }
            };

            graphConfig.padding = {top: 10, bottom: 20, left: 50, right: 20};
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

        function getFrom() {
            var from = new Date($scope.selection);
            from.setDate(from.getDate() - ($scope.numberOfPeriods - 1));
            return from;
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var graphDataUrl = 'rest/elektriciteit/verbruikPerDag/' + getFrom().getTime() + '/' + $scope.selection.getTime();
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
