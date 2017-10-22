(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagEnergieHistorieController', DagEnergieHistorieController);

    DagEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$filter', '$timeout', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function DagEnergieHistorieController($scope, $routeParams, $location, $http, $log, $filter, $timeout, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {
        var ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
        var HALF_DAY_IN_MILLISECONDS = 12 * 60 * 60 * 1000;

        $scope.changeSoort = changeSoort;
        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;
        $scope.isMaxSelected = isMaxSelected;
        $scope.navigate = navigate;
        $scope.toggleDatepickerPopup = toggleDatepickerPopup;
        $scope.selectionChange = selectionChange;
        $scope.navigateToDetailsOfSelection = navigateToDetailsOfSelection;

        $scope.period = 'dag';
        $scope.soort = $routeParams.verbruiksoort;
        $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
        $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
        $scope.dateformat = 'MMMM yyyy';
        $scope.datepickerPopupOptions = { datepickerMode: 'month', minMode: 'month', maxDate: Date.today() };
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
                $scope.selection = dateProvidedByLocation.moveToFirstDayOfMonth();
            } else {
                $scope.selection = Date.today().moveToFirstDayOfMonth();

            }
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
            return Date.today().getMonth() === $scope.selection.getMonth() && Date.today().getFullYear() === $scope.selection.getFullYear();
        }

        function navigate(numberOfPeriods) {
            var date = $scope.selection.clone().add(numberOfPeriods).months();
            var formattedDate = EnergieHistorieService.formatDateForLocationSearch(date);
            $location.search('datum', formattedDate);
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            getDataFromServer();
        }

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
            chartConfig.data.order = function(data1, data2) { return data2.id.localeCompare(data1.id); };
            chartConfig.data.colors = chartConfig.data.colors = EnergieHistorieService.getDataColors();
            chartConfig.data.onclick = function (data, element) { navigateToDetailsOfSelection(data.x, true); };

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
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
                return d3.time.format('%d-%m (%a)')(new Date(d.datumtijd));
            };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter, 'datumtijd');
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

                var van = $scope.selection.getTime();
                var totEnMet = $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

                var dataUrl = 'api/energie/verbruik-per-dag/' + van + '/' + totEnMet;
                $http({method: 'GET', url: dataUrl}).then(
                    function successCallback(response) {
                        loadData(EnergieHistorieService.transformServerdata(response.data, 'datumtijd'));
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
