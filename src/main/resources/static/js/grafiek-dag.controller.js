(function() {
    'use strict';

    angular
        .module('app')
        .controller('DagGrafiekController', DagGrafiekController);

    DagGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$q', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function DagGrafiekController($scope, $routeParams, $http, $q, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        var ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
        var HALF_DAY_IN_MILLISECONDS = 12 * 60 * 60 * 1000;

        activate();

        function activate() {
            $scope.selection = Date.today().moveToFirstDayOfMonth();
            $scope.period = 'dag';
            $scope.soort = $routeParams.soort;
            $scope.energiesoorten = GrafiekService.getEnergiesoorten($scope.soort);
            $scope.supportedsoorten = GrafiekService.getSupportedSoorten();

            LocalizationService.localize();
            GrafiekService.manageGraphSize($scope);

            getDataFromServer();
        }

        $scope.toggleEnergiesoort = function (energieSoortToToggle) {
            if ($scope.allowMultpleEnergiesoorten()) {
                var index = $scope.energiesoorten.indexOf(energieSoortToToggle);
                if (index >= 0) {
                    $scope.energiesoorten.splice(index, 1);
                } else {
                    $scope.energiesoorten.push(energieSoortToToggle);
                }
                getDataFromServer();
            } else {
                if ($scope.energiesoorten[0] != energieSoortToToggle) {
                    $scope.energiesoorten = [energieSoortToToggle];
                    getDataFromServer();
                }
            }
        };

        $scope.allowMultpleEnergiesoorten = function() {
            return $scope.soort == 'kosten';
        };

        $scope.getD3DateFormat = function() {
            return '%B %Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            viewMode: 'months',
            minViewMode: 'months',
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

        datepicker.on('changeDate', function(e) {
            if (!Date.equals(e.date, $scope.selection)) {
                $log.info("changeDate event from datepicker. Selected date: " + e.date);

                $scope.$apply(function() {
                    $scope.selection = e.date;
                    getDataFromServer();
                });
            }
        });

        $scope.isMaxSelected = function() {
            return Date.today().getMonth() == $scope.selection.getMonth() && Date.today().getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection.setMonth($scope.selection.getMonth() + numberOfPeriods);
            datepicker.datepicker('setDate', $scope.selection);
            getDataFromServer();
        };

        function getTicksForEveryDayInMonth() {
            var tickValues = [];

            var numberOfDaysInMonth = Date.getDaysInMonth($scope.selection.getFullYear(), $scope.selection.getMonth());
            for (var i = 0; i < numberOfDaysInMonth; i++) {
                tickValues.push($scope.selection.getTime() + (i * ONE_DAY_IN_MILLISECONDS));
            }
            return tickValues;
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 20, left: 55, right: 20}
            }
        }

        function getGraphConfig(data) {
            var graphConfig = {};

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = data;
            graphConfig.data.type = 'bar';
            graphConfig.data.order = null;
            graphConfig.data.colors = graphConfig.data.colors = GrafiekService.getDataColors();

            var keysGroups = [];
            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                keysGroups.push($scope.energiesoorten[i] + "-" + $scope.soort);
            }
            graphConfig.data.groups = [keysGroups];
            graphConfig.data.keys = {x: 'dt', value: keysGroups};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: 'timeseries',
                tick: {format: '%a %d', values: getTicksForEveryDayInMonth(), centered: true, multiline: true, width: 25},
                min: $scope.selection.getTime() - HALF_DAY_IN_MILLISECONDS,
                max: $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999),
                padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return GrafiekService.formatWithoutUnitLabel($scope.soort, value); };
            graphConfig.axis.y = {tick: {format: yAxisFormat }};
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var $$ = this;
                    var config = $$.config;
                    var CLASS = $$.CLASS;
                    var tooltipContents;
                    var total = 0;

                    var orderAsc = false;
                    if (config.data_groups.length === 0) {
                        d.sort(function(a,b){
                            return orderAsc ? a.value - b.value : b.value - a.value;
                        });
                    } else {
                        var ids = $$.orderTargets($$.data.targets).map(function (i) {
                            return i.id;
                        });
                        d.sort(function(a, b) {
                            if (a.value > 0 && b.value > 0) {
                                return orderAsc ? ids.indexOf(a.id) - ids.indexOf(b.id) : ids.indexOf(b.id) - ids.indexOf(a.id);
                            } else {
                                return orderAsc ? a.value - b.value : b.value - a.value;
                            }
                        });
                    }

                    for (i = 0; i < d.length; i++) {
                        if (!(d[i] && (d[i].value || d[i].value === 0))) { continue; }

                        if (!tooltipContents) {
                            var formatter = d3.time.format('%a %d-%m');
                            var title = formatter(d[i].x);
                            tooltipContents = "<table class='" + $$.CLASS.tooltip + "'>" + "<tr><th colspan='2'>" + title + "</th></tr>";
                        }

                        var formattedName = (d[i].name.charAt(0).toUpperCase() + d[i].name.slice(1)).replace('-verbruik', '').replace('-kosten', '');
                        var formattedValue = GrafiekService.formatWithUnitLabel($scope.soort, $scope.energiesoorten, d[i].value);
                        var bgcolor = $$.levelColor ? $$.levelColor(d[i].value) : color(d[i].id);

                        tooltipContents += "<tr class='" + CLASS.tooltipName + "-" + d[i].id + "'>";
                        tooltipContents += "<td class='name'><span style='background-color:" + bgcolor + "; border-radius: 5px;'></span>" + formattedName + "</td>";
                        tooltipContents += "<td class='value'>" + formattedValue + "</td>";
                        tooltipContents += "</tr>";

                        total += d[i].value;
                    }

                    if (d.length > 1) {
                        tooltipContents += "<tr class='" + CLASS.tooltipName + "'>";
                        tooltipContents += "<td class='name'><strong>Totaal</strong></td>";
                        tooltipContents += "<td class='value'><strong>" + GrafiekService.formatWithUnitLabel($scope.soort, $scope.energiesoorten, total) + "</strong></td>";
                        tooltipContents += "</tr>";
                    }
                    tooltipContents += "</table>";

                    return tooltipContents;
                }
            };

            graphConfig.padding = {top: 10, bottom: 20, left: 50, right: 20};
            graphConfig.grid = {y: {show: true}};

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;
            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            var labelFormatter = function(d) {
                var formatter = d3.time.format('%d-%m (%a)');
                return formatter(new Date(d.dt))
            };
            var table = GrafiekService.getTableData(data, $scope.energiesoorten, $scope.soort, labelFormatter);
            $scope.rows = table.rows;
            $scope.cols = table.cols;
        }

        function loadDataIntoGraph(data) {
            var graphConfig = data.length == 0 ? getEmptyGraphConfig() : getGraphConfig(data);
            $scope.chart = c3.generate(graphConfig);
            GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function transformServerdata(serverresponses) {
            var result = [];

            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                var energiesoort = $scope.energiesoorten[i];
                var serverdataForEnergiesoort = serverresponses[i].data;

                for (var j = 0; j < serverdataForEnergiesoort.length; j++) {
                    var dataOnDt = getByDt(result, serverdataForEnergiesoort[j].dt);

                    if (dataOnDt == null) {
                        dataOnDt = {};
                        result.push(dataOnDt);
                    }
                    dataOnDt['dt'] = serverdataForEnergiesoort[j].dt;

                    for (var k = 0; k < $scope.supportedsoorten.length; k++) {
                        var soort = $scope.supportedsoorten[k].code;
                        dataOnDt[energiesoort + '-' + soort] = serverdataForEnergiesoort[j][soort];
                    }
                }
            }
            return result;
        }

        function getByDt(data, dt) {
            for (var i = 0; i < data.length; i++) {
                if (data[i].dt == dt) {
                    return data[i];
                }
            }
            return null;
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var van = $scope.selection.getTime();
                var totEnMet = $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

                var requests = [];

                for (var i = 0; i < $scope.energiesoorten.length; i++) {
                    var dataUrl = 'rest/' + $scope.energiesoorten[i] + '/verbruik-per-dag/' + van + '/' + totEnMet;
                    $log.info('Getting data from URL: ' + dataUrl);
                    requests.push( $http({method: 'GET', url: dataUrl}) );
                }

                $q.all(requests).then(
                    function successCallback(response) {
                        loadData(transformServerdata(response));
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        $log.error("ERROR: " + JSON.stringify(response));
                        LoadingIndicatorService.stopLoading();
                    }
                );
            }
        }
    }

})();
