(function() {
    'use strict';

    angular
        .module('app')
        .controller('UurEnergieHistorieController', UurEnergieHistorieController);

    UurEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$filter', '$timeout', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function UurEnergieHistorieController($scope, $routeParams, $location, $http, $log, $filter, $timeout, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {

        $scope.selectionChange = selectionChange;
        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;
        $scope.isMaxSelected = isMaxSelected;
        $scope.navigate = navigate;
        $scope.toggleDatepickerPopup = toggleDatepickerPopup;
        $scope.selectionChange = selectionChange;
        $scope.navigateToDetailsOfSelection = navigateToDetailsOfSelection;

        $scope.period = 'uur';
        $scope.soort = $routeParams.verbruiksoort;
        $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
        $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
        $scope.dateformat = 'EEE. dd-MM-yyyy';
        $scope.datepickerPopupOptions = { maxDate: Date.today() };
        $scope.datepickerPopup = { opened: false };
        $scope.data = [];

        activate();

        function activate() {
            EnergieHistorieService.manageChartSize($scope, showChart, showTable);
            setSelection();
            getDataFromServer();
        }

        function showChart() {
            if (!$scope.showChart) {
                $scope.showTable = false;
                $scope.showChart = true;
                loadDataIntoChart($scope.data);
            }
        }

        function showTable() {
            if (!$scope.showTable) {
                $scope.showChart = false;
                $scope.showTable = true;
                loadDataIntoTable($scope.data);
            }
        }

        function setSelection() {
            var dateProvidedByLocation = Date.parseExact($location.search().datum, 'dd-MM-yyyy');
            if (dateProvidedByLocation) {
                $scope.selection = dateProvidedByLocation;
            } else {
                $scope.selection = Date.today();

            }
        }

        function changePeriod(period) {
            $location.path('energie/' + $scope.soort + '/' + period).search('energiesoort', $scope.energiesoorten);
        }

         function toggleEnergiesoort(energiesoortToToggle) {
            if (EnergieHistorieService.toggleEnergiesoort($scope.energiesoorten, energiesoortToToggle, $scope.allowMultpleEnergiesoorten())) {
                $location.search('energiesoort', $scope.energiesoorten);
            }
        }

        function allowMultpleEnergiesoorten() {
            return $scope.soort === 'kosten';
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

        function getChartConfig(data) {
            var chartConfig = {};

            chartConfig.bindto = '#chart';

            chartConfig.data = {};
            chartConfig.data.json = data;
            chartConfig.data.type = 'bar';
            chartConfig.data.order = function(data1, data2) { return data2.id.localeCompare(data1.id); };
            chartConfig.data.colors = EnergieHistorieService.getDataColors();
            chartConfig.data.onclick = function (data, element) { navigateToDetailsOfSelection($scope.selection); };

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'uur', value: keysGroups};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: 'category',
                tick: {
                    format: function (value) { return formatAsHourPeriodLabel(value); }
                }
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 0.8}};
            chartConfig.transition = {duration: 0};

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    return EnergieHistorieService.getTooltipContent(this, d, defaultTitleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
                }
            };

            chartConfig.padding = EnergieHistorieService.getChartPadding();
            chartConfig.grid = {y: {show: true}};

            return chartConfig;
        }

        function loadData(data) {
            $scope.data = data;
            if ($scope.showChart) {
                loadDataIntoChart(data);
            }
            if ($scope.showTable) {
                loadDataIntoTable(data);
            }
        }

        function formatAsHourPeriodLabel(uur) {
            return numbro(uur).format('00') + ':00 - ' + numbro(uur + 1).format('00') + ':00';
        }

        function loadDataIntoTable(data) {
            $log.debug('loadDataIntoTable', data.length);

            var labelFormatter = function(d) { return formatAsHourPeriodLabel(d.uur); };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter, 'uur');
            $scope.rows = table.rows;
            $scope.cols = table.cols;
        }

        function loadDataIntoChart(data) {
            var chartConfig = data.length === 0 ? EnergieHistorieService.getEmptyChartConfig() : getChartConfig(data);
            $scope.chart = c3.generate(chartConfig);
            EnergieHistorieService.setChartHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var dataUrl = 'api/energie/verbruik-per-uur-op-dag/' + $scope.selection.getTime();

                $http({method: 'GET', url: dataUrl}).then(
                    function successCallback(response) {
                        loadData(EnergieHistorieService.transformServerdata(response.data, 'uur'));
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

        function navigateToDetailsOfSelection() {
            $timeout(function() {
                $location.path('/energie/stroom/opgenomen-vermogen/').search('datum', $filter('date')($scope.selection, 'dd-MM-yyyy'));
            });
        }
    }
})();
