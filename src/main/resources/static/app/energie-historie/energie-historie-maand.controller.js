(function() {
    'use strict';

    angular
        .module('app')
        .controller('MaandEnergieHistorieController', MaandEnergieHistorieController);

    MaandEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$timeout', 'LoadingIndicatorService', 'DATETIME_CONSTANTS', 'EnergieHistorieService', 'ErrorMessageService'];

    function MaandEnergieHistorieController($scope, $routeParams, $location, $http, $log, $timeout, LoadingIndicatorService, DATETIME_CONSTANTS, EnergieHistorieService, ErrorMessageService) {

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
            $scope.period = 'maand';
            $scope.soort = $routeParams.verbruiksoort;
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
            $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
            $scope.datepickerPopupOptions = { datepickerMode: 'year', minMode: 'year', maxDate: Date.today() };
            $scope.datepickerPopup = { opened: false };
            $scope.dateformat = 'yyyy';
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
            return $scope.selection && Date.today().getFullYear() === $scope.selection.getFullYear();
        }

        function navigate(numberOfPeriods) {
            var date = $scope.selection.clone().add(numberOfPeriods).years();
            var formattedDate = EnergieHistorieService.formatDateForLocationSearch(date);
            $location.search('datum', formattedDate);
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            changeDate($scope.selected);
        }

        function getTicksForEveryMonthInYear() {
            return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
        }

        function chartDataClick(data, element) {
            navigateToDetailsOfSelection(data.x);
        }

        function getChartConfig(data) {
            var chartConfig = EnergieHistorieService.getDefaultBarChartConfig(data);

            chartConfig.data.onclick = chartDataClick;

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'maand', value: keysGroups};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                tick: {
                    format: function (d) {
                        return DATETIME_CONSTANTS.shortMonths[d - 1];
                    },
                    values: getTicksForEveryMonthInYear(), xcentered: true
                }
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = function(d) { return DATETIME_CONSTANTS.fullMonths[d - 1]; };
                    return EnergieHistorieService.getTooltipContent(this, d, titleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
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

        function loadDataIntoTable(data) {
            $log.debug('loadDataIntoTable', data.length);

            var labelFormatter = function(d) { return DATETIME_CONSTANTS.fullMonths[d.maand - 1]; };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter, 'maand');
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

                var dataUrl = 'api/energie/verbruik-per-maand-in-jaar/' + $scope.selection.getFullYear();
                $http({method: 'GET', url: dataUrl}).then(
                    function successCallback(response) {
                        loadData(EnergieHistorieService.transformServerdata(response.data, 'maand'));
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

        function navigateToDetailsOfSelection(monthNumber) {
            $timeout(function() {
                var date = Date.parseExact('1-' + monthNumber + '-' + $scope.selection.getFullYear(), 'd-M-yyyy');
                var formattedDate = EnergieHistorieService.formatDateForLocationSearch(date);
                $location.path('/energie/' + $scope.soort + '/dag').search({energiesoort: $scope.energiesoorten, datum: formattedDate});
            });
        }
    }
})();
