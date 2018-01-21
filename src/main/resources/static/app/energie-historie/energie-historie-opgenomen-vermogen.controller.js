(function() {
    'use strict';

    angular
        .module('app')
        .controller('OpgenomenVermogenGrafiekController', OpgenomenVermogenGrafiekController);

    OpgenomenVermogenGrafiekController.$inject = ['$scope', '$http', '$log', '$location', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function OpgenomenVermogenGrafiekController($scope, $http, $log, $location, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {
        var THREE_MINUTES_IN_SECONDS = 3 * 60;

        $scope.isMaxSelected = isMaxSelected;
        $scope.navigate = navigate;
        $scope.toggleDatepickerPopup = toggleDatepickerPopup;
        $scope.selectionChange = selectionChange;

        $scope.data = [];
        $scope.energiesoort = 'stroom';
        $scope.period = 'opgenomen-vermogen'; // Strange but true for this controller :-o
        $scope.supportedsoorten = [{'code': 'stroom', 'omschrijving': 'Watt'}];
        $scope.soort = 'stroom';
        $scope.dateformat = 'EEE. dd-MM-yyyy';
        $scope.historicDataDisplayType = 'chart';
        $scope.datepickerPopupOptions = { maxDate: Date.today() };
        $scope.datepickerPopup = { opened: false };
        $scope.showChart = true; // This controller always shows data in chart, never in table
        $scope.hideUur = true;
        $scope.hideDag = true;
        $scope.hideMaand = true;
        $scope.hideJaar = true;
        $scope.hideEnergieSoorten = true;
        $scope.showOpgenomenVermogen = true;

        activate();

        function activate() {
            EnergieHistorieService.manageChartSize($scope, doNothing, doNothing);
            setSelection();
            getDataFromServer();
        }

        function doNothing() {
            // Does nothing
        }

        function setSelection() {
            var dateProvidedByLocation = Date.parseExact($location.search().datum, 'dd-MM-yyyy');
            if (dateProvidedByLocation) {
                $scope.selection = dateProvidedByLocation;
            } else {
                $scope.selection = Date.today();
            }
        }

        function isMaxSelected() {
            return Date.today().getTime() === $scope.selection.getTime();
        }

        function navigate(numberOfPeriods) {
            $scope.selection = $scope.selection.clone().add(numberOfPeriods).days();
            getDataFromServer();
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            getDataFromServer();
        }

        function getTicksForEveryHourInPeriod(from, to) {
            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;

            var tickValues = [];
            for (var i = 0; i <= numberOfHoursInDay; i++) {
                var tickValue = from.getTime() + (i * 60 * 60 * 1000);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function transformData(data) {
            var transformedData = [];
            var length = data.length;
            var previousTarief = null;

            for (var i = 0; i < length; i++) {
                var transformedDataItem = {};

                var tarief = data[i].tariefIndicator.toLowerCase();
                transformedDataItem.datumtijd = new Date(data[i].datumtijd).getTime();
                transformedDataItem['watt-' + tarief] = data[i].watt;

                // Fill the "gap" between this row and the previous one
                if (previousTarief && tarief && tarief !== previousTarief) {
                    var obj = {};
                    obj.datumtijd = new Date(data[i].datumtijd).getTime() - 1;
                    var attribute = 'watt-' + previousTarief;
                    obj[attribute] = data[i].watt;
                    transformedData.push(obj);
                }

                previousTarief = tarief;
                transformedData.push(transformedDataItem);
            }
            return transformedData;
        }

        function getStatistics(chartData) {
            var nonZero = _.filter(chartData, function(o) { return o.watt !== null && o.watt > 0; } );

            var mean = _.meanBy(nonZero, 'watt');
            var min = _.minBy(nonZero, 'watt');
            var max = _.maxBy(chartData, 'watt');

            return {
                avg: mean,
                min: _.isUndefined(min) ? undefined : min.watt,
                max: _.isUndefined(max) ? undefined : max.watt
            };
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

        function getChartConfig(transformedChartData, statistics) {
            var chartConfig = {};
            var tickValues = getTicksForEveryHourInPeriod($scope.selection, getTo());

            chartConfig.bindto = '#chart';

            chartConfig.data = {json: transformedChartData, keys: {x: 'datumtijd', value: ['watt-dal', 'watt-normaal']}, types: {'watt-dal': 'area', 'watt-normaal': 'area'}};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -30},
                min: $scope.selection, max: getTo(),
                padding: {left: 0, right: 10}
            };
            chartConfig.legend = {show: false};
            chartConfig.point = {show: false};
            chartConfig.transition = {duration: 0};
            chartConfig.tooltip = {show: false};
            chartConfig.padding = getChartPadding();
            chartConfig.grid = {y: {show: true}};

            chartConfig.grid.y.lines = EnergieHistorieService.getStatisticsChartLines(statistics, formatWithUnitLabel);

            return chartConfig;
        }

        function loadDataIntoChart(chartData) {
            var statistics = getStatistics(chartData);
            $scope.chartData = transformData(chartData);

            var chartConfig;
            if ($scope.chartData.length === 0) {
                chartConfig = getEmptyChartConfig();
            } else {
                chartConfig = getChartConfig($scope.chartData, statistics);
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

            var dataUrl = 'api/opgenomen-vermogen/historie/' + $scope.selection.toString('yyyy-MM-dd') + '/' + getTo().toString('yyyy-MM-dd') + '?subPeriodLength=' + THREE_MINUTES_IN_SECONDS;

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
