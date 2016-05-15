(function() {
    'use strict';

    angular
        .module('app')
        .controller('MaandEnergieHistorieController', MaandEnergieHistorieController);

    MaandEnergieHistorieController.$inject = ['$scope', '$routeParams', '$http', '$q', '$log', 'LoadingIndicatorService', 'LocalizationService', 'EnergieHistorieService', 'ErrorMessageService'];

    function MaandEnergieHistorieController($scope, $routeParams, $http, $q, $log, LoadingIndicatorService, LocalizationService, EnergieHistorieService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.selection = d3.time.format('%d-%m-%Y').parse('01-01-'+(Date.today().getFullYear()));
            $scope.period = 'maand';
            $scope.soort = $routeParams.soort;
            $scope.energiesoorten = EnergieHistorieService.getEnergiesoorten($scope.soort);
            $scope.supportedsoorten = EnergieHistorieService.getSupportedSoorten();

            LocalizationService.localize();
            EnergieHistorieService.manageGraphSize($scope);

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
            return Date.today().getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection.setFullYear($scope.selection.getFullYear() + numberOfPeriods);
            datepicker.datepicker('setDate', $scope.selection);
            getDataFromServer();
        };

        $scope.getD3DateFormat = function() {
            return '%Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            viewMode: 'years', minViewMode: 'years', autoclose: true, todayHighlight: true, endDate: "0d", language:"nl",
            format: {
                toDisplay: function (date, format, language) {
                    return d3.time.format($scope.getD3DateFormat())(date);
                },
                toValue: function (date, format, language) {
                    return (date == '0d' ? new Date() : d3.time.format($scope.getD3DateFormat()).parse(date));
                }
            }
        });

        datepicker.datepicker('setDate', $scope.selection);

        datepicker.on('changeDate', function(e) {
            $log.info("changeDate event from datepicker. Selected date: " + e.date);

            if (!Date.equals(e.date, $scope.selection)) {
                $scope.$apply(function() {
                    $scope.selection = e.date;
                    getDataFromServer();
                });
            }
        });

        function getTicksForEveryMonthInYear() {
            return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
        }

        function getGraphPadding() {
            return {top: 10, bottom: 25, left: 55, right: 20};
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: getGraphPadding()
            }
        }

        function getGraphConfig(data) {
            var graphConfig = {};

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = data;
            graphConfig.data.type = 'bar';
            graphConfig.data.order = null;
            graphConfig.data.colors = EnergieHistorieService.getDataColors();

            var keysGroups = [];
            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                keysGroups.push($scope.energiesoorten[i] + "-" + $scope.soort);
            }
            graphConfig.data.groups = [keysGroups];
            graphConfig.data.keys = {x: 'maand', value: keysGroups};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                tick: {
                    format: function (d) {
                        return LocalizationService.getShortMonths()[d - 1];
                    },
                    values: getTicksForEveryMonthInYear(), xcentered: true
                }, min: 0.5, max: 2.5, padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return EnergieHistorieService.formatWithoutUnitLabel($scope.soort, value); };
            graphConfig.axis.y = {tick: {format: yAxisFormat }};
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var titleFormat = function(d) { return LocalizationService.getFullMonths()[d - 1]; };
                    return EnergieHistorieService.getTooltipContent(this, d, titleFormat, defaultValueFormat, color, $scope.soort, $scope.energiesoorten);
                }
            };
            graphConfig.padding = getGraphPadding();
            graphConfig.grid = {y: {show: true}};

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;
            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            var labelFormatter = function(d) { return LocalizationService.getFullMonths()[d.maand - 1] };
            var table = EnergieHistorieService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter);
            $scope.rows = table.rows;
            $scope.cols = table.cols;
        }

        function loadDataIntoGraph(data) {
            var graphConfig = data.length == 0 ? getEmptyGraphConfig() : getGraphConfig(data);
            $scope.chart = c3.generate(graphConfig);
            EnergieHistorieService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function transformServerdata(serverresponses) {
            return EnergieHistorieService.transformServerdata(serverresponses, 'maand', $scope.energiesoorten, $scope.supportedsoorten);
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var requests = [];

                for (var i = 0; i < $scope.energiesoorten.length; i++) {
                    var dataUrl = 'rest/' +  $scope.energiesoorten[i] + '/verbruik-per-maand-in-jaar/' + $scope.selection.getFullYear();
                    $log.info('Getting data from URL: ' + dataUrl);
                    requests.push( $http({method: 'GET', url: dataUrl}) );
                }

                $q.all(requests).then(
                    function successCallback(response) {
                        loadData(transformServerdata(response));
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        $log.error(JSON.stringify(response));
                        LoadingIndicatorService.stopLoading();
                        ErrorMessageService.showMessage("Er is een fout opgetreden bij het ophalen van de gegevens");
                    }
                );
            }
        }
    }
})();
