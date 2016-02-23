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
            $scope.selection = (new Date()).clearTime().moveToFirstDayOfMonth();

            $scope.energiesoort = $routeParams.energiesoort;
            $scope.verbruikLabel = GrafiekService.getVerbruikLabel($scope.energiesoort);
            $scope.period = 'dag';
            $scope.soort = GrafiekService.getSoortData();
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': GrafiekService.getVerbruikLabel($scope.energiesoort)}, {'code': 'kosten', 'omschrijving': '\u20AC'}];

            LocalizationService.localize();
            GrafiekService.manageGraphSize($scope);

            loadData([]);
            getDataFromServer();
        }

        $scope.getD3DateFormat = function() {
            return '%b %Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            viewMode: 'months',
            minViewMode: 'months',
            autoclose: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
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
            return (new Date()).getMonth() == $scope.selection.getMonth() && (new Date()).getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = new Date($scope.selection);
            $scope.selection.setMonth($scope.selection.getMonth() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            datepicker.datepicker('setDate', $scope.selection);

            getDataFromServer();
        };

        $scope.switchSoort = function(destinationSoortCode) {
            $scope.soort = destinationSoortCode;
            $scope.verbruikLabel = GrafiekService.getVerbruikLabel($scope.energiesoort);
            GrafiekService.setSoortData(destinationSoortCode);

            loadData($scope.data);
        };

        function getTicksForEveryDayInMonth() {
            var tickValues = [];

            var numberOfDaysInMonth = Date.getDaysInMonth($scope.selection.getFullYear(), $scope.selection.getMonth());
            for (var i = 0; i < numberOfDaysInMonth; i++) {
                var tickValue = $scope.selection.getTime() + (i * ONE_DAY_IN_MILLISECONDS);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 20, left: 55, right: 20}
            }
        }

        function getGraphConfig(data) {
            var graphConfig = {};

            var tickValues = getTicksForEveryDayInMonth();

            var xMin = $scope.selection.getTime() - HALF_DAY_IN_MILLISECONDS;
            var xMax = (new Date($scope.selection)).moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = data;
            graphConfig.data.type = 'bar';
            graphConfig.data.keys = {x: 'dt', value: [$scope.soort]};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: 'timeseries',
                tick: {format: '%a %d', values: tickValues, centered: true, multiline: true, width: 25},
                min: xMin,
                max: xMax,
                padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return GrafiekService.formatWithoutUnitLabel($scope.energiesoort, value); };
            graphConfig.axis.y = {tick: {format: yAxisFormat }};
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                format: {
                    name: function (name, ratio, id, index) {
                        return $scope.soort.charAt(0).toUpperCase() + $scope.soort.slice(1);
                    },
                    value: function (value, ratio, id) {
                        return GrafiekService.formatWithUnitLabel($scope.energiesoort, value);
                    }
                }
            };

            graphConfig.padding = {top: 10, bottom: 20, left: 50, right: 20};
            graphConfig.grid = {y: {show: true}};
            graphConfig.grid.y.lines = GrafiekService.getStatisticsGraphGridYLines(data, $scope.energiesoort);

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;

            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            $scope.tableData = [];

            for (var i = 0; i < data.length; i++) {
                var formatter = d3.time.format('%d-%m (%a)');
                var label = formatter(new Date(data[i].dt));

                var verbruik = '';
                var kosten = '';

                if (data[i].verbruik != null) {
                    verbruik = GrafiekService.formatWithoutUnitLabel('kosten', data[i].verbruik);
                }
                if (data[i].kosten != null) {
                    kosten = GrafiekService.formatWithoutUnitLabel('verbruik', data[i].kosten);
                }
                $scope.tableData.push({label: label, verbruik: verbruik, kosten: kosten});
            }
        }

        function loadDataIntoGraph(data) {
            var graphConfig;
            if (data.length == 0) {
                graphConfig = getEmptyGraphConfig();
            } else {
                graphConfig = getGraphConfig(data);
            }
            $scope.chart = c3.generate(graphConfig);
            GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var van = $scope.selection.getTime();
            var totEnMet = (new Date($scope.selection)).moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            var dataUrl = 'rest/' + $scope.energiesoort + '/verbruik-per-dag/' + van + '/' + totEnMet;
            $log.info('Getting data from URL: ' + dataUrl);

            $http({
                method: 'GET', url: dataUrl
            }).then(function successCallback(response) {
                loadData(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
            });
        }
    }

})();
