(function() {
    'use strict';

    angular
        .module('app')
        .controller('UurEnergieHistorieController', UurEnergieHistorieController);

    UurEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$timeout', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function UurEnergieHistorieController($scope, $routeParams, $location, $http, $log, $timeout, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {

        $scope.changeSoort = changeSoort;
        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;
        $scope.isMaxSelected = isMaxSelected;
        $scope.navigate = navigate;
        $scope.toggleDatepickerPopup = toggleDatepickerPopup;
        $scope.selectionChange = selectionChange;
        $scope.navigateToDetailsOfSelection = navigateToDetailsOfSelection;

        activate();

        function activate() {
            if (!getDateFromLocationSearch()) {
                changeDate(Date.today());
                return;
            }

            $scope.selection = getDateFromLocationSearch();
            $scope.period = 'uur';
            $scope.soort = $routeParams.verbruiksoort;
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
            $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
            $scope.dateformat = 'EEE. dd-MM-yyyy';
            $scope.datepickerPopupOptions = { maxDate: Date.today() };
            $scope.datepickerPopup = { opened: false };
            $scope.data = [];

            EnergieHistorieService.manageChartSize($scope, showChart, showTable);
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

        function getDateFromLocationSearch() {
            return Date.parseExact($location.search().datum, 'dd-MM-yyyy');
        }

        function changeDate(date) {
            var formattedDate = EnergieHistorieService.formatDateForLocationSearch(date);
            $location.search('datum', formattedDate);
        }

        function changeSoort(soort) {
            $location.path('energie/' + soort + '/' + $scope.period).search('energiesoort', null);
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
            return $scope.selection && Date.today().getTime() === $scope.selection.getTime();
        }

        function navigate(numberOfPeriods) {
            var date = $scope.selection.clone().add(numberOfPeriods).days();
            changeDate(date);
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            changeDate($scope.selection);
        }

        function chartDataClick(data, element) {
            navigateToDetailsOfSelection(data.x);
        }

        function getChartConfig(data) {
            var chartConfig = EnergieHistorieService.getDefaultBarChartConfig(data);

            chartConfig.data.onclick = chartDataClick;

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

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    return EnergieHistorieService.getTooltipContent(this, d, defaultTitleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
                }
            };

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

                var dataUrl = 'api/energie/verbruik-per-uur-op-dag/' + $scope.selection.toString('yyyy-MM-dd');

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
                var formattedDate = EnergieHistorieService.formatDateForLocationSearch($scope.selection);
                $location.path('/energie/stroom/opgenomen-vermogen/').search('datum', formattedDate);
            });
        }
    }
})();
