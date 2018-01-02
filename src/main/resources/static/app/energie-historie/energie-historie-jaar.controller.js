(function() {
    'use strict';

    angular
        .module('app')
        .controller('JaarEnergieHistorieController', JaarEnergieHistorieController);

    JaarEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$filter', '$timeout', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function JaarEnergieHistorieController($scope, $routeParams, $location, $http, $log, $filter, $timeout, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {

        $scope.changeSoort = changeSoort;
        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.navigateToDetailsOfSelection = navigateToDetailsOfSelection;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;

        activate();

        function activate() {
            $scope.selection = d3.time.format('%d-%m-%Y').parse('01-01-' + (Date.today().getFullYear()));
            $scope.period = 'jaar';
            $scope.soort = $routeParams.verbruiksoort;
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
            $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
            $scope.hideDateSelection = true;
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

        function chartDataClick(data, element) {
            navigateToDetailsOfSelection(data.x);
        }

        function getChartConfig(data) {
            var chartConfig = EnergieHistorieService.getDefaultBarChartConfig(data);

            chartConfig.data.onclick = chartDataClick;

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'jaar', value: keysGroups};

            chartConfig.axis = {};

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};

            chartConfig.tooltip = {
                contents: function (data, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = function(d) { return d; };
                    return EnergieHistorieService.getTooltipContent(this, data, titleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
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

            var labelFormatter = function(d) { return d.jaar; };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter, 'jaar');
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

                var dataUrl = 'api/energie/verbruik-per-jaar';
                $http({method: 'GET', url: dataUrl}).then(
                    function successCallback(response) {
                        loadData(EnergieHistorieService.transformServerdata(response.data, 'jaar'));
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

        function navigateToDetailsOfSelection(year) {
            $timeout(function() {
                console.log(year);
                var date = Date.parseExact('1-1-' + year, 'd-M-yyyy');
                $location.path('/energie/' + $scope.soort + '/maand').search({energiesoort: $scope.energiesoorten, datum: $filter('date')(date, "dd-MM-yyyy")});
            });
        }
    }
})();
