(function() {
    'use strict';

    angular
        .module('app')
        .controller('MaandGrafiekController', MaandGrafiekController);

    MaandGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function MaandGrafiekController($scope, $routeParams, $http, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        activate();

        function activate() {
            $scope.selection = d3.time.format('%d-%m-%Y').parse('01-01-'+(new Date()).getFullYear());

            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'kWh'}, {'code': 'kosten', 'omschrijving': '\u20AC'}];
            $scope.energiesoort = $routeParams.energiesoort;
            $scope.period = 'maand';
            $scope.soort = GrafiekService.getSoortData();

            LocalizationService.localize();
            GrafiekService.manageGraphSize($scope);

            clearGraph();
            getDataFromServer();
        }

        $scope.isMaxSelected = function() {
            return (new Date()).getFullYear() == $scope.selection.getFullYear();
        };

        $scope.showNumberOfPeriodsSelector = function() {
            return false;
        };

        $scope.switchSoort = function(destinationSoortCode) {
            $scope.soort = destinationSoortCode;
            GrafiekService.setSoortData(destinationSoortCode);
            loadDataIntoGraph($scope.graphData);
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = new Date($scope.selection);
            $scope.selection.setFullYear($scope.selection.getFullYear() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            datepicker.datepicker('setDate', $scope.selection);

            getDataFromServer();
        };

        $scope.getD3DateFormat = function() {
            return '%Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            viewMode: 'years',
            minViewMode: 'years',
            autoclose: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            format: {
                toDisplay: function (date, format, language) {
                    var formatter = d3.time.format($scope.getD3DateFormat());
                    return formatter(date);
                },
                toValue: function (date, format, language) {
                    if (date == '0d') {
                        return new Date();
                    }
                    return d3.time.format($scope.getD3DateFormat()).parse(date);
                }
            }
        });

        datepicker.datepicker('setDate', $scope.selection);
        var applyDatePickerUpdatesInAngularScope = true;

        datepicker.on('changeDate', function(e) {
            $log.info("changeDate event from datepicker. Selected date: " + e.date);

            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    getDataFromServer();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });

        function getTicksForEveryMonthInYear() {
            var tickValues = [];
            for (var i = 1; i <= 12; i++) {
                tickValues.push(i);
            }
            return tickValues;
        }

        function getStatistics(graphData) {
            var min;
            var max;
            var avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < graphData.length; i++) {
                var data;

                if ($scope.soort == 'verbruik') {
                    data = graphData[i].kWh;
                } else if ($scope.soort == 'kosten') {
                    data = graphData[i].euro;
                }

                if (data != null && (typeof max=='undefined' || data > max)) {
                    max = data;
                }
                if (data != null && (typeof min=='undefined' || data < min)) {
                    min = data;
                }
                if (data != null) {
                    total += data;
                    nrofdata += 1;
                }
            }

            if (nrofdata > 0) {
                avg = total / nrofdata;
            }
            return {avg: avg, min: min, max: max};
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 10, left: 50, right: 20}
            }
        }

        function formatBasedOnSoort(value) {
            if ($scope.soort == 'verbruik') {
                return Math.round(value) + ' kWh';
            } else if ($scope.soort == 'kosten') {
                var format = d3.format(".2f");
                return '\u20AC ' + format(value);
            }
        }
        function getGraphConfig(graphData) {
            var graphConfig = {};

            var tickValues = getTicksForEveryMonthInYear();

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = graphData;
            graphConfig.data.type = 'bar';

            var value;
            if ($scope.soort == 'verbruik') {
                value = 'kWh';
            } else if ($scope.soort == 'kosten') {
                value = 'euro';
            }
            graphConfig.data.keys = {x: 'maand', value: [value]};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                tick: {
                    format: function (d) {
                        return LocalizationService.getShortMonths()[d - 1];
                    }, values: tickValues, xcentered: true
                }, min: 0.5, max: 2.5, padding: {left: 0, right: 10}
            };

            if ($scope.soort == 'kosten') {
                graphConfig.axis.y = {tick: {format: d3.format(".2f")}};
            }

            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            var soortOmschrijving;
            if ($scope.soort == 'verbruik') {
                soortOmschrijving = 'kWh';
            } else if ($scope.soort == 'kosten') {
                soortOmschrijving = '\u20AC';
            }
            graphConfig.tooltip = {
                format: {
                    title: function (d) {
                        return LocalizationService.getFullMonths()[d - 1];
                    },
                    name: function (name, ratio, id, index) {
                        return $scope.soort.charAt(0).toUpperCase() + $scope.soort.slice(1);
                    },
                    value: function (value, ratio, id) {
                        return formatBasedOnSoort(value);
                    }
                }
            };
            graphConfig.padding = {top: 10, bottom: 10, left: 50, right: 20};

            graphConfig.grid = {y: {show: true}};

            var statistics = getStatistics(graphData);

            var lines = [];
            if (statistics.avg) {
                lines.push({value: statistics.avg, text: 'Gemiddelde: ' + formatBasedOnSoort(statistics.avg), class: 'avg', position: 'middle'});
            }
            if (statistics.min) {
                lines.push({value: statistics.min, text: 'Laagste: ' + formatBasedOnSoort(statistics.min), class: 'min', position: 'start'});
            }
            if (statistics.max) {
                lines.push({value: statistics.max, text: 'Hoogste: ' + formatBasedOnSoort(statistics.max), class: 'max'});
            }
            graphConfig.grid.y.lines = lines;

            return graphConfig;
        }

        function clearGraph() {
            loadDataIntoGraph([]);
        }

        function loadDataIntoGraph(graphData) {
            $scope.graphData = graphData;

            var graphConfig;
            if (graphData.length == 0) {
                graphConfig = getEmptyGraphConfig();
            } else {
                graphConfig = getGraphConfig(graphData);
            }
            $scope.chart = c3.generate(graphConfig);
            GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var graphDataUrl = 'rest/' +  $scope.energiesoort + '/verbruikPerMaandInJaar/' + $scope.selection.getFullYear();
            $log.info('Getting data for graph from URL: ' + graphDataUrl);

            $http({
                method: 'GET', url: graphDataUrl
            }).then(function successCallback(response) {
                loadDataIntoGraph(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
            });
        }
    }

})();
