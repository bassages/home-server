(function() {
    'use strict';

    angular
        .module('app')
        .controller('MaandEnergieHistorieController', MaandEnergieHistorieController);

    MaandEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$filter', 'LoadingIndicatorService', 'DATETIME_CONSTANTS', 'EnergieHistorieService', 'ErrorMessageService'];

    function MaandEnergieHistorieController($scope, $routeParams, $location, $http, $log, $filter, LoadingIndicatorService, DATETIME_CONSTANTS, EnergieHistorieService, ErrorMessageService) {

        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;
        $scope.isMaxSelected = isMaxSelected;
        $scope.navigate = navigate;
        $scope.toggleDatepickerPopup = toggleDatepickerPopup;
        $scope.selectionChange = selectionChange;

        $scope.period = 'maand';

        $scope.soort = $routeParams.verbruiksoort;
        $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();

        $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);

        $scope.datepickerPopupOptions = { datepickerMode: 'year', minMode: 'year', maxDate: Date.today() };
        $scope.datepickerPopup = { opened: false };
        $scope.dateformat = 'yyyy';

        $scope.data = [];

        activate();

        function activate() {
            EnergieHistorieService.manageChartSize($scope);

            var year;
            var dateProvidedByLocation = Date.parse($location.search().datum);
            if (dateProvidedByLocation) {
                year = dateProvidedByLocation.getFullYear();
            } else {
                year = Date.today().getFullYear();
            }

            $scope.selection = Date.parseExact('1-1-' + year, 'd-M-yyyy');

            $scope.$watch('showChart', function(newValue, oldValue) {
                if (newValue !== oldValue && newValue) {
                    loadDataIntoChart($scope.data);
                }
            });
            $scope.$watch('showTable', function(newValue, oldValue) {
                if (newValue !== oldValue && newValue) {
                    loadDataIntoTable($scope.data);
                }
            });
            getDataFromServer();
        }

        function changePeriod(period) {
            $location.path('energie/' + $scope.soort + '/' + period).search('energiesoort', $scope.energiesoorten);
        }

        function toggleEnergiesoort (energiesoortToToggle) {
            if (EnergieHistorieService.toggleEnergiesoort($scope.energiesoorten, energiesoortToToggle, $scope.allowMultpleEnergiesoorten())) {
                $location.search('energiesoort', $scope.energiesoorten);
            }
        }

        function allowMultpleEnergiesoorten() {
            return $scope.soort === 'kosten';
        }

        function isMaxSelected() {
            return Date.today().getFullYear() === $scope.selection.getFullYear();
        }

        function navigate(numberOfPeriods) {
            $scope.selection = $scope.selection.clone().add(numberOfPeriods).years();
            getDataFromServer();
        }

        function toggleDatepickerPopup() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        }

        function selectionChange() {
            getDataFromServer();
        }

        function getTicksForEveryMonthInYear() {
            return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
        }

        function getChartConfig(data) {
            var chartConfig = {};

            chartConfig.bindto = '#chart';

            chartConfig.data = {};
            chartConfig.data.json = data;
            chartConfig.data.type = 'bar';
            chartConfig.data.order = function(data1, data2) { return data2.id.localeCompare(data1.id); };
            chartConfig.data.colors = EnergieHistorieService.getDataColors();
            chartConfig.data.onclick = function (data, element) { navigateToDaysInMonth(Date.parseExact('1-' + data.x + '-' + $scope.selection.getFullYear(), 'd-M-yyyy')); };

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
                }, min: 0.5, max: 2.5, padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 0.8}};
            chartConfig.transition = {duration: 0};

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = function(d) { return DATETIME_CONSTANTS.fullMonths[d - 1]; };
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

            var labelFormatter = function(d) { return DATETIME_CONSTANTS.fullMonths[d.maand - 1]; };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter);
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

        function navigateToDaysInMonth(dateOfMonth) {
            $location.path('/energie/' + $scope.soort + '/dag').search({energiesoort: $scope.energiesoorten, datum: $filter('date')(dateOfMonth, "dd-MM-yyyy")});
            $scope.$apply();
        }
    }
})();
