(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagEnergieHistorieController', DagEnergieHistorieController);

    DagEnergieHistorieController.$inject = ['$scope', '$routeParams', '$http', '$q', '$log', 'LoadingIndicatorService', 'LocalizationService', 'EnergieHistorieService', 'ErrorMessageService'];

    function DagEnergieHistorieController($scope, $routeParams, $http, $q, $log, LoadingIndicatorService, LocalizationService, EnergieHistorieService, ErrorMessageService) {
        var ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
        var HALF_DAY_IN_MILLISECONDS = 12 * 60 * 60 * 1000;

        activate();

        function activate() {
            $scope.selection = Date.today().moveToFirstDayOfMonth();
            $scope.period = 'dag';
            $scope.soort = $routeParams.soort;
            $scope.energiesoorten = EnergieHistorieService.getEnergiesoorten($scope.soort);
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
            $scope.dateformat = 'MMMM yyyy';

            LocalizationService.localize();
            EnergieHistorieService.manageChartSize($scope);

            getDataFromServer();
        }

        $scope.toggleEnergiesoort = function (energiesoortToToggle) {
            if (EnergieHistorieService.toggleEnergiesoort($scope.energiesoorten, energiesoortToToggle, $scope.allowMultpleEnergiesoorten())) {
                getDataFromServer();
            }
        };

        $scope.allowMultpleEnergiesoorten = function() {
            return $scope.soort == 'kosten';
        };

        $scope.isMaxSelected = function() {
            return Date.today().getMonth() == $scope.selection.getMonth() && Date.today().getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = $scope.selection.clone().add(numberOfPeriods).months();
            getDataFromServer();
        };

        $scope.datepickerPopupOptions = {
            datepickerMode: 'month',
            minMode: 'month',
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

        function getTicksForEveryDayInMonth() {
            var tickValues = [];

            var numberOfDaysInMonth = Date.getDaysInMonth($scope.selection.getFullYear(), $scope.selection.getMonth());
            for (var i = 0; i < numberOfDaysInMonth; i++) {
                tickValues.push($scope.selection.getTime() + (i * ONE_DAY_IN_MILLISECONDS));
            }
            return tickValues;
        }

        function getChartConfig(data) {
            var chartConfig = {};

            chartConfig.bindto = '#chart';

            chartConfig.data = {};
            chartConfig.data.json = data;
            chartConfig.data.type = 'bar';
            chartConfig.data.order = null;
            chartConfig.data.colors = chartConfig.data.colors = EnergieHistorieService.getDataColors();

            var keysGroups = [];
            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                keysGroups.push($scope.energiesoorten[i] + "-" + $scope.soort);
            }
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'datumtijd', value: keysGroups};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: 'timeseries',
                tick: {format: '%a %d', values: getTicksForEveryDayInMonth(), centered: true, multiline: true, width: 25},
                min: $scope.selection.getTime() - HALF_DAY_IN_MILLISECONDS,
                max: $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999),
                padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 0.8}};
            chartConfig.transition = {duration: 0};

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = d3.time.format('%a %d-%m');
                    return EnergieHistorieService.getTooltipContent(this, d, titleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
                }
            };

            chartConfig.padding = EnergieHistorieService.getChartPadding();
            chartConfig.grid = {y: {show: true}};

            return chartConfig;
        }

        function loadData(data) {
            $scope.data = data;
            loadDataIntoChart(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            var labelFormatter = function(d) {
                return d3.time.format('%d-%m (%a)')(new Date(d.datumtijd));
            };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter);
            $scope.rows = table.rows;
            $scope.cols = table.cols;
        }

        function loadDataIntoChart(data) {
            var chartConfig = data.length === 0 ? EnergieHistorieService.getEmptyChartConfig() : getChartConfig(data);
            $scope.chart = c3.generate(chartConfig);
            EnergieHistorieService.setChartHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function transformServerdata(serverresponses) {
            return EnergieHistorieService.transformServerdata(serverresponses, 'datumtijd', $scope.energiesoorten, $scope.supportedsoorten);
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var van = $scope.selection.getTime();
                var totEnMet = $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

                var requests = [];

                for (var i = 0; i < $scope.energiesoorten.length; i++) {
                    var dataUrl = 'api/' + $scope.energiesoorten[i] + '/verbruik-per-dag/' + van + '/' + totEnMet;
                    requests.push( $http({method: 'GET', url: dataUrl}) );
                }

                $q.all(requests).then(
                    function successCallback(response) {
                        loadData(transformServerdata(response));
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
    }
})();
