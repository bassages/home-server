(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagGrafiekController', DagGrafiekController);

    DagGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function DagGrafiekController($scope, $routeParams, $http, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        var ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
        var HALF_DAY_IN_MILLISECONDS = 12 * 60 * 60 * 1000;

        activate();

        function activate() {
            var today = new Date();
            today.setHours(0,0,0,0);
            $scope.selection = today;

            $scope.numberOfPeriods = 7;

            $scope.energiesoort = $routeParams.energiesoort;
            $scope.period = 'dag';
            $scope.soort = GrafiekService.getSoortData();
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'kWh'}, {'code': 'kosten', 'omschrijving': '\u20AC'}];

            LocalizationService.localize();
            GrafiekService.manageGraphSize($scope);

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

        $scope.navigate = function(numberOfPeriods) {
            var selection = new Date($scope.selection);
            selection.setDate($scope.selection.getDate() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            datepicker.datepicker('setDate', selection);

            $scope.selection = selection;
            getDataFromServer();
        };

        $scope.switchSoort = function(destinationSoortCode) {
            $scope.soort = destinationSoortCode;
            GrafiekService.setSoortData(destinationSoortCode);
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
                var tickValue = $scope.selection.getTime() - (i * ONE_DAY_IN_MILLISECONDS);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function getStatistics(graphData) {
            var min;
            var max;
            var avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < graphData.length; i++) {
                var data;

                if ($scope.soort == 'verbruik') {
                    data = graphData[i].kWh;
                } else if ($scope.soort == 'kosten') {
                    data = graphData[i].euro;
                }

                if (data != null && (typeof max=='undefined' || data > max)) {
                    max = data;
                }
                if (data != null && (typeof min=='undefined' || data < min)) {
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

        function formatBasedOnSoort(value) {
            if ($scope.soort == 'verbruik') {
                return Math.round(value) + ' kWh';
            } else if ($scope.soort == 'kosten') {
                var format = d3.format(".2f");
                return '\u20AC ' + format(value);
            }
        }

        function getGraphConfig(graphData) {
            var graphConfig = {};

            var tickValues = getTicksForEveryDayInPeriod();

            var xMin = new Date(getFrom().getTime()) - HALF_DAY_IN_MILLISECONDS;
            var xMax = new Date($scope.selection.getTime() + HALF_DAY_IN_MILLISECONDS);

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
                        return formatBasedOnSoort(value);
                    }
                }
            };

            graphConfig.padding = {top: 10, bottom: 20, left: 50, right: 20};
            graphConfig.grid = {y: {show: true}};

            var statistics = getStatistics(graphData);

            var lines = [];
            if (statistics.avg) {
                lines.push({value: statistics.avg, text: 'Gemiddelde: ' + formatBasedOnSoort(statistics.avg), class: 'avg', position: 'middle'});
            }
            if (statistics.min) {
                lines.push({value: statistics.min, text: 'Laagste: ' + formatBasedOnSoort(statistics.min), class: 'min', position: 'start'});
            }
            if (statistics.max) {
                lines.push({value: statistics.max, text: 'Hoogste: ' + formatBasedOnSoort(statistics.max), class: 'max'});
            }
            graphConfig.grid.y.lines = lines;

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

        function getFrom() {
            var from = new Date($scope.selection);
            from.setDate(from.getDate() - ($scope.numberOfPeriods - 1));
            return from;
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var graphDataUrl = 'rest/' + $scope.energiesoort + '/verbruikPerDag/' + getFrom().getTime() + '/' + $scope.selection.getTime();
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
