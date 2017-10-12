(function() {
    'use strict';

    angular
        .module('app')
        .controller('JaarEnergieHistorieController', JaarEnergieHistorieController);

    JaarEnergieHistorieController.$inject = ['$scope', '$routeParams', '$location', '$http', '$log', '$filter', 'LoadingIndicatorService', 'EnergieHistorieService', 'ErrorMessageService'];

    function JaarEnergieHistorieController($scope, $routeParams, $location, $http, $log, $filter, LoadingIndicatorService, EnergieHistorieService, ErrorMessageService) {

        $scope.changePeriod = changePeriod;
        $scope.toggleEnergiesoort = toggleEnergiesoort;
        $scope.allowMultpleEnergiesoorten = allowMultpleEnergiesoorten;

        $scope.selection = d3.time.format('%d-%m-%Y').parse('01-01-'+(Date.today().getFullYear()));
        $scope.period = 'jaar';
        $scope.soort = $routeParams.verbruiksoort;
        $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();
        $scope.energiesoorten = EnergieHistorieService.getEnergieSoorten($location.search(), $scope.soort);
        $scope.hideDateSelection = true;
        $scope.data = [];

        activate();

        function activate() {
            EnergieHistorieService.manageChartSize($scope);

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

        function toggleEnergiesoort(energiesoortToToggle) {
            if (EnergieHistorieService.toggleEnergiesoort($scope.energiesoorten, energiesoortToToggle, $scope.allowMultpleEnergiesoorten())) {
                $location.search('energiesoort', $scope.energiesoorten);
            }
        }

        function allowMultpleEnergiesoorten() {
            return $scope.soort === 'kosten';
        }

        function getTicksForEveryYearInResponse(data) {
            return _.map(data, 'jaar').sort();
        }

        function getChartConfig(data) {
            var chartConfig = {};

            chartConfig.bindto = '#chart';

            chartConfig.data = {};
            chartConfig.data.json = data;
            chartConfig.data.type = 'bar';
            chartConfig.data.order = function(data1, data2) { return data2.id.localeCompare(data1.id); };
            chartConfig.data.colors = EnergieHistorieService.getDataColors();
            chartConfig.data.onclick = function (data, element) { navigateToMonthsInYear(Date.parseExact('1-1-' + data.x, 'd-M-yyyy')); };

            var keysGroups = EnergieHistorieService.getKeysGroups($scope.energiesoorten, $scope.soort);
            chartConfig.data.groups = [keysGroups];
            chartConfig.data.keys = {x: 'jaar', value: keysGroups};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                tick: {
                    format: function (d) { return d; },
                    values: getTicksForEveryYearInResponse(data), xcentered: true
                }, padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            chartConfig.axis.y = {tick: {format: yAxisFormat }};
            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 0.8}};
            chartConfig.transition = {duration: 0};

            chartConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = function(d) { return d; };
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

            var labelFormatter = function(d) { return d.jaar; };
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

        function navigateToMonthsInYear(dateOfYear) {
            $location.path('/energie/' + $scope.soort + '/maand').search({energiesoort: $scope.energiesoorten, datum: $filter('date')(dateOfYear, "dd-MM-yyyy")});
            $scope.$apply();
        }
    }
})();
