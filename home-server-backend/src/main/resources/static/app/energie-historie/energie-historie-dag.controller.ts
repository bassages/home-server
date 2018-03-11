(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagEnergieHistorieController', DagEnergieHistorieController);

    DagEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$timeout', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function DagEnergieHistorieController($scope, $routeParams, $location, $http, $log, $timeout, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {

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
            $scope.period = 'dag';
            $scope.soort = $routeParams.verbruiksoort;
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
            $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
            $scope.dateformat = 'MMMM yyyy';
            $scope.datepickerPopupOptions = { datepickerMode: 'month', minMode: 'month', maxDate: Date.today() };
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
            const formattedDate = EnergieHistorieService.formatDateForLocationSearch(date);
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
            return $scope.selection &&
                Date.today().getMonth() === $scope.selection.getMonth() &&
                Date.today().getFullYear() === $scope.selection.getFullYear();
        }

        function navigate(numberOfPeriods) {
            var date = getPeriodStartDate().add(numberOfPeriods).months();
            changeDate(date);
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            changeDate($scope.selection);
        }

        function getTicksForEveryDayInMonth() {
            var tickValues = [];

            var date = getPeriodStartDate();

            var numberOfDaysInMonth = Date.getDaysInMonth($scope.selection.getFullYear(), $scope.selection.getMonth());
            for (var i = 0; i < numberOfDaysInMonth; i++) {
                tickValues.push(date.getTime());
                date.addDays(1);
            }
            return tickValues;
        }

        function chartDataClick(data, element) {
            navigateToDetailsOfSelection(data.x);
        }

        function getChartConfig(data) {
            var chartConfig = EnergieHistorieService.getDefaultBarChartConfig(data);

            chartConfig.data.onclick = chartDataClick;

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'dag', value: keysGroups};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: 'timeseries',
                tick: {format: '%a %d', values: getTicksForEveryDayInMonth(), centered: true, multiline: true, width: 25},
                min: getPeriodStartDate().addHours(-12),
                max: getPeriodEndDate().addHours(-12),
                padding: {left: 0, right: 0}
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};

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
            if ($scope.showChart) {
                loadDataIntoChart(data);
            }
            if ($scope.showTable) {
                loadDataIntoTable(data);
            }
        }

        function loadDataIntoTable(data) {
            $log.debug('loadDataIntoTable', data.length);

            var labelFormatter = function(d) {
                return d3.time.format('%d-%m (%a)')(new Date(d.dag));
            };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter, 'dag');
            $scope.rows = table.rows;
            $scope.cols = table.cols;
        }

        function loadDataIntoChart(data) {
            var chartConfig = data.length === 0 ? EnergieHistorieService.getEmptyChartConfig() : getChartConfig(data);
            $scope.chart = c3.generate(chartConfig);
            EnergieHistorieService.setChartHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getPeriodStartDate() {
            return $scope.selection.clone().moveToFirstDayOfMonth();
        }

        function getPeriodEndDate() {
            return new Date($scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999));
        }

        function getPeriodToDate() {
            return new Date($scope.selection.clone().addMonths(1));
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var from = getPeriodStartDate();
                var to = getPeriodToDate();

                var dataUrl = 'api/energie/verbruik-per-dag/' + from.toString('yyyy-MM-dd') + '/' + to.toString('yyyy-MM-dd');
                $http({method: 'GET', url: dataUrl}).then(
                    function successCallback(response) {
                        loadData(EnergieHistorieService.transformServerdata(response.data, 'dag'));
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

        function navigateToDetailsOfSelection(datumtijd) {
            $timeout(function() {
                var formattedDate = EnergieHistorieService.formatDateForLocationSearch(datumtijd);
                $location.path('/energie/' + $scope.soort + '/uur').search({energiesoort: $scope.energiesoorten, datum: formattedDate});
            });
        }
    }
})();
