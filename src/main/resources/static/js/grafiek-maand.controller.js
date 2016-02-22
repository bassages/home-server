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

            $scope.energiesoort = $routeParams.energiesoort;
            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': GrafiekService.getVerbruikLabel($scope.energiesoort)}, {'code': 'kosten', 'omschrijving': '\u20AC'}];
            $scope.period = 'maand';
            $scope.soort = GrafiekService.getSoortData();

            LocalizationService.localize();
            GrafiekService.manageGraphSize($scope);

            loadData([]);
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
            loadData($scope.data);
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

        function getStatistics(data) {
            var min;
            var max;
            var avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < data.length; i++) {
                var value;

                if ($scope.soort == 'verbruik') {
                    value = data[i].verbruik;
                } else if ($scope.soort == 'kosten') {
                    value = data[i].euro;
                }

                if (value != null && (typeof max=='undefined' || value > max)) {
                    max = value;
                }
                if (value != null && (typeof min=='undefined' || value < min)) {
                    min = value;
                }
                if (value != null) {
                    total += value;
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

        function getGraphConfig(data) {
            var graphConfig = {};

            var tickValues = getTicksForEveryMonthInYear();

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = data;
            graphConfig.data.type = 'bar';

            graphConfig.data.keys = {x: 'maand', value: [$scope.soort]};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                tick: {
                    format: function (d) {
                        return LocalizationService.getShortMonths()[d - 1];
                    }, values: tickValues, xcentered: true
                }, min: 0.5, max: 2.5, padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return GrafiekService.formatWithoutUnitLabel($scope.energiesoort, value); };
            graphConfig.axis.y = {tick: {format: yAxisFormat }};
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                format: {
                    title: function (d) {
                        return LocalizationService.getFullMonths()[d - 1];
                    },
                    name: function (name, ratio, id, index) {
                        return $scope.soort.charAt(0).toUpperCase() + $scope.soort.slice(1);
                    },
                    value: function (value, ratio, id) {
                        return GrafiekService.formatWithUnitLabel($scope.energiesoort, value);
                    }
                }
            };
            graphConfig.padding = {top: 10, bottom: 10, left: 55, right: 20};

            graphConfig.grid = {y: {show: true}};

            var statistics = getStatistics(data);

            var lines = [];
            if (statistics.avg) {
                lines.push({value: statistics.avg, text: 'Gemiddelde: ' + GrafiekService.formatWithUnitLabel($scope.energiesoort, statistics.avg), class: 'avg', position: 'middle'});
            }
            if (statistics.min) {
                lines.push({value: statistics.min, text: 'Laagste: ' + GrafiekService.formatWithUnitLabel($scope.energiesoort, statistics.min), class: 'min', position: 'start'});
            }
            if (statistics.max) {
                lines.push({value: statistics.max, text: 'Hoogste: ' + GrafiekService.formatWithUnitLabel($scope.energiesoort, statistics.max), class: 'max'});
            }
            graphConfig.grid.y.lines = lines;

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;

            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            $scope.tableData = [];

            for (var i = 0; i < data.length; i++) {
                var label = LocalizationService.getFullMonths()[data[i].maand - 1];

                var verbruik = '';
                var kosten = '';

                if (data[i].verbruik != null) {
                    verbruik = GrafiekService.formatWithoutUnitLabel('kosten', data[i].verbruik);
                }
                if (data[i].kosten != null) {
                    kosten = GrafiekService.formatWithoutUnitLabel('verbruik', data[i].kosten);
                }
                $scope.tableData.push({label: label, verbruik: verbruik, kosten: kosten});
            }
        }
        function loadDataIntoGraph(data) {
            $scope.data = data;

            var graphConfig;
            if (data.length == 0) {
                graphConfig = getEmptyGraphConfig();
            } else {
                graphConfig = getGraphConfig(data);
            }
            $scope.chart = c3.generate(graphConfig);
            GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var dataUrl = 'rest/' +  $scope.energiesoort + '/verbruik-per-maand-in-jaar/' + $scope.selection.getFullYear();
            $log.info('Getting data from URL: ' + dataUrl);

            $http({
                method: 'GET', url: dataUrl
            }).then(function successCallback(response) {
                loadData(response.data);
                LoadingIndicatorService.stopLoading();
            }, function errorCallback(response) {
                LoadingIndicatorService.stopLoading();
            });
        }
    }

})();

