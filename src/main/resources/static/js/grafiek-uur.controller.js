(function() {
    'use strict';

    angular
        .module('app')
        .controller('UurGrafiekController', UurGrafiekController);

    UurGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$q', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function UurGrafiekController($scope, $routeParams, $http, $q, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        activate();

        function activate() {
            $scope.selection = Date.today();
            $scope.period = 'uur';
            $scope.soort = $routeParams.soort;
            $scope.energiesoorten = GrafiekService.getEnergiesoorten($scope.soort);
            $scope.supportedsoorten = GrafiekService.getSupportedSoorten();

            GrafiekService.manageGraphSize($scope);
            LocalizationService.localize();

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
            return '%a %d-%m-%Y';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            daysOfWeekHighlighted: "0,6",
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
            return Date.today().getTime() == $scope.selection.getTime();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection.setDate($scope.selection.getDate() + numberOfPeriods);
            datepicker.datepicker('setDate', $scope.selection);
            getDataFromServer();
        };

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 20, left: 50, right: 20}
            }
        }

        function getGraphConfig(data) {
            var graphConfig = {};

            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.json = data;
            graphConfig.data.type = 'bar';
            graphConfig.data.order = null;
            graphConfig.data.colors = GrafiekService.getDataColors();

            var keysGroups = [];
            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                keysGroups.push($scope.energiesoorten[i] + "-" + $scope.soort);
            }
            graphConfig.data.groups = [keysGroups];
            graphConfig.data.keys = {x: 'uur', value: keysGroups};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: 'category',
                tick: {
                    format: function (value) { return formatAsHourPeriodLabel(value) }
                }
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
                            var title = defaultTitleFormat(d[i].x);
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

            graphConfig.padding = {top: 10, bottom: 45, left: 55, right: 20};
            graphConfig.grid = {y: {show: true}};

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;
            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function formatAsHourPeriodLabel(uur) {
            return numbro(uur).format('00') + ':00 - ' + numbro(uur + 1).format('00') + ':00';
        }

        function loadDataIntoTable(data) {
            var labelFormatter = function(d) { return formatAsHourPeriodLabel(d.uur) };
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
            return GrafiekService.transformServerdata(serverresponses, 'uur', $scope.energiesoorten, $scope.supportedsoorten);
        }

        function getDataFromServer() {
            loadData([]);

            if ($scope.energiesoorten.length > 0) {
                LoadingIndicatorService.startLoading();

                var requests = [];

                for (var i = 0; i < $scope.energiesoorten.length; i++) {
                    var dataUrl = 'rest/' + $scope.energiesoorten[i] + '/verbruik-per-uur-op-dag/' + $scope.selection.getTime();
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
