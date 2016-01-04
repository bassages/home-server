'use strict';

// TODO: https://github.com/eternicode/bootstrap-datepicker/issues/615

angular.module('appHomecontrol.dagGrafiekController', [])

    .controller('DagGrafiekController', ['$scope', '$routeParams', '$http', '$log', 'SharedDataService', 'LocalizationService', 'GrafiekWindowSizeService', function($scope, $routeParams, $http, $log, SharedDataService, LocalizationService, GrafiekWindowSizeService) {
        var oneDay = 24 * 60 * 60 * 1000;
        var halfDay = 12 * 60 * 60 * 1000;

        $scope.loading = false;
        $scope.period = 'dag';
        $scope.energiesoort = $routeParams.energiesoort;
        $scope.periode = $routeParams.periode;
        $scope.soort = SharedDataService.getSoortData();
        $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'kWh'}, {'code': 'kosten', 'omschrijving': '\u20AC'}];

        // By default, today is the last day in the graph
        $scope.selection = new Date();
        $scope.selection.setHours(0,0,0,0);
        $scope.numberOfPeriods = 7;

        LocalizationService.localize();
        GrafiekWindowSizeService.manage($scope);

        var applyDatePickerUpdatesInAngularScope = false;
        var theDatepicker = $('.datepicker');
        theDatepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
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
                    $scope.getDataFromServer();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });
        theDatepicker.datepicker('setDate', $scope.selection);

        Date.CultureInfo.abbreviatedDayNames = LocalizationService.getShortDays();

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
            $scope.selection = selection;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', selection);
            $scope.getDataFromServer();
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
                $scope.getDataFromServer();
            }
        };

        function getTicksForEveryDayInPeriod() {
            var tickValues = [];
            for (var i = 0; i <= ($scope.numberOfPeriods-1); i++) {
                var tickValue = $scope.selection.getTime() - (i * oneDay);
                tickValues.push(tickValue);
                $log.debug('Add tick for ' + new Date(tickValue));
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

        function loadDataIntoGraph(graphData) {
            $scope.graphData = graphData;

            var tickValues = getTicksForEveryDayInPeriod();
            var average = getAverage(graphData);

            var xMin = new Date(getFrom().getTime()) - halfDay;
            var xMax = new Date($scope.selection.getTime() + halfDay);

            var graphConfig = {};
            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = graphData;
            graphConfig.data.type = 'bar';

            if ($scope.soort == 'verbruik') {
                graphConfig.data.keys = {x: 'dt', value: ['kWh']};
            } else if ($scope.soort == 'kosten') {
                graphConfig.data.keys = {x: 'dt', value: ['euro']};
            }

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
            graphConfig.point = {show: false};
            graphConfig.transition = {duration: 0};
            graphConfig.grid = {y: {show: true}};

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

            graphConfig.padding = {top: 10, bottom: 20, left: 45, right: 20};
            if (average > 0) {
                graphConfig.grid.y.lines = [{value: average, text: '', class: 'gemiddelde'}];
            }
            $scope.chart = c3.generate(graphConfig);
            GrafiekWindowSizeService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getFrom() {
            var from = new Date($scope.selection);
            from.setDate(from.getDate() - ($scope.numberOfPeriods - 1));
            return from;
        }

        $scope.getDataFromServer = function() {
            $scope.loading = true;

            var graphDataUrl = 'rest/elektriciteit/verbruikPerDag/' + getFrom().getTime() + '/' + $scope.selection.getTime();
            $log.info('URL: ' + graphDataUrl);

            $http({
                method: 'GET', url: graphDataUrl
            }).then(function successCallback(response) {
                loadDataIntoGraph(response.data);
                $scope.loading = false;
            }, function errorCallback(response) {
                $scope.loading = false;
            });
        }
    }]);
