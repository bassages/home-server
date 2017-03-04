(function() {
    'use strict';

    angular
        .module('app')
        .controller('OpgenomenVermogenGrafiekController', OpgenomenVermogenGrafiekController);

    OpgenomenVermogenGrafiekController.$inject = ['$scope', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'EnergieHistorieService', 'ErrorMessageService'];

    function OpgenomenVermogenGrafiekController($scope, $http, $log, LoadingIndicatorService, LocalizationService, EnergieHistorieService, ErrorMessageService) {
        var THREE_MINUTES_IN_MILLISECONDS = 3 * 60 * 1000;

        activate();

        function activate() {
            $scope.selection = Date.today();
            $scope.energiesoort = 'stroom';
            $scope.period = 'opgenomen-vermogen'; // Strange but true for this controller :-o
            $scope.supportedsoorten = [{'code': 'stroom', 'omschrijving': 'Watt'}];
            $scope.soort = 'stroom';
            $scope.dateformat = 'EEE. dd-MM-yyyy';
            $scope.historicDataDisplayType = 'chart';
            EnergieHistorieService.manageChartSize($scope);
            LocalizationService.localize();

            getDataFromServer();
        }

        $scope.hideEnergieSoorten = function() {
            return true;
        };

        $scope.showOpgenomenVermogen = function() {
            return true;
        };

        $scope.hideUur = function() {
            return true;
        };

        $scope.hideDag = function() {
            return true;
        };

        $scope.hideMaand = function() {
            return true;
        };

        $scope.isMaxSelected = function() {
            return Date.today().getTime() == $scope.selection.getTime();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = $scope.selection.clone().add(numberOfPeriods).days();
            getDataFromServer();
        };

        $scope.datepickerPopupOptions = {
            maxDate: Date.today()
        };

        $scope.datepickerPopup = {
            opened: false
        };

        $scope.toggleDatepickerPopup = function() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        };

        $scope.selectionChange = function() {
            getDataFromServer();
        };

        function getTicksForEveryHourInPeriod(from, to) {
            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;

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
                var subPeriodEnd = data[i].dt + (THREE_MINUTES_IN_MILLISECONDS - 1);
                data.push({dt: subPeriodEnd, watt: data[i].watt});
            }
        }

        function getStatistics(chartData) {
            var min, max, avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < chartData.length; i++) {
                var data = chartData[i].watt;

                if (data !== null && (typeof max=='undefined' || data > max)) {
                    max = data;
                }
                if (data !== null && data > 0 && (typeof min=='undefined' || data < min)) {
                    min = data;
                }
                if (data !== null && data > 0) {
                    total += data;
                    nrofdata += 1;
                }
            }

            if (nrofdata > 0) {
                avg = total / nrofdata;
            }
            return {avg: avg, min: min, max: max};
        }

        function formatWithUnitLabel(value) {
            return value === null ? null : Math.round(value) + ' watt';
        }

        function getChartPadding() {
            return {top: 10, bottom: 25, left: 55, right: 20};
        }

        function getEmptyChartConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 30, left: 50, right: 20}
            };
        }

        function getChartConfig(chartData) {
            var chartConfig = {};
            var tickValues = getTicksForEveryHourInPeriod($scope.selection, getTo());

            chartConfig.bindto = '#chart';

            chartConfig.data = {json: chartData, keys: {x: "dt", value: ["watt"]}, types: {"watt": "area"}};
            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -30},
                min: $scope.selection, max: getTo(),
                padding: {left: 0, right: 10}
            };
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 1}};
            chartConfig.point = {show: false};
            chartConfig.transition = {duration: 0};
            chartConfig.tooltip = {show: false};
            chartConfig.padding = getChartPadding();
            chartConfig.grid = {y: {show: true}};

            var statistics = getStatistics(chartData);

            chartConfig.grid.y.lines = EnergieHistorieService.getStatisticsChartLines(statistics, formatWithUnitLabel);

            addSubPeriodEnd(chartData);

            return chartConfig;
        }

        function loadDataIntoChart(chartData) {
            $scope.chartData = chartData;

            var chartConfig;
            if (chartData.length === 0) {
                chartConfig = getEmptyChartConfig();
            } else {
                chartConfig = getChartConfig(chartData);
            }
            $scope.chart = c3.generate(chartConfig);
            EnergieHistorieService.setChartHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo() {
            return $scope.selection.clone().add({ days: 1});
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();
            loadDataIntoChart([]);

            var dataUrl = 'api/' + $scope.energiesoort + '/opgenomen-vermogen-historie/' + $scope.selection.getTime() + '/' + getTo().getTime() + '?subPeriodLength=' + THREE_MINUTES_IN_MILLISECONDS;
            $log.info('Getting data from URL: ' + dataUrl);

            $http({method: 'GET', url: dataUrl})
                .then(
                    function successCallback(response) {
                        loadDataIntoChart(response.data);
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        $log.error(angular.toJson(response));
                        LoadingIndicatorService.stopLoading();
                        ErrorMessageService.showMessage("Er is een fout opgetreden bij het ophalen van de gegevens");
                    }
                );
        }
    }
})();
