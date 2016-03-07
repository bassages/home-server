(function() {
    'use strict';

    angular
        .module('app')
        .controller('MaandGrafiekController', MaandGrafiekController);

    MaandGrafiekController.$inject = ['$scope', '$routeParams', '$http', '$q', '$log', 'LoadingIndicatorService', 'LocalizationService', 'GrafiekService'];

    function MaandGrafiekController($scope, $routeParams, $http, $q, $log, LoadingIndicatorService, LocalizationService, GrafiekService) {
        activate();

        function activate() {
            $scope.selection = d3.time.format('%d-%m-%Y').parse('01-01-'+(Date.today().getFullYear()));
            $scope.period = 'maand';
            $scope.soort = $routeParams.soort;

            if ($scope.soort == 'kosten') {
                $scope.energiesoorten = ['stroom', 'gas'];
            } else {
                $scope.energiesoorten = ['stroom'];
            }

            $scope.supportedsoorten = [{'code': 'verbruik', 'omschrijving': 'Verbruik'}, {'code': 'kosten', 'omschrijving': 'Kosten'}];

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
            var tickValues = [];
            for (var i = 1; i <= 12; i++) {
                tickValues.push(i);
            }
            return tickValues;
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

            graphConfig.data.colors = {
                'stroom-verbruik': '#4575B3',
                'stroom-kosten': '#4575B3',
                'gas-verbruik': '#BA2924',
                'gas-kosten': '#BA2924'
            };

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
                    }, values: tickValues, xcentered: true
                }, min: 0.5, max: 2.5, padding: {left: 0, right: 10}
            };

            var yAxisFormat = function (value) { return GrafiekService.formatWithoutUnitLabel($scope.soort, value); };
            graphConfig.axis.y = {tick: {format: yAxisFormat }};
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.transition = {duration: 0};

            graphConfig.tooltip = {
                contents: function (d, defaultTitleFormat, defaultValueFormat, color) {
                    var $$ = this, config = $$.config, CLASS = $$.CLASS, tooltipContents, total = 0;

                    for (i = 0; i < d.length; i++) {
                        if (!(d[i] && (d[i].value || d[i].value === 0))) { continue; }

                        if (!tooltipContents) {
                            var title = LocalizationService.getFullMonths()[(d[i].x) - 1];
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
            graphConfig.padding = {top: 10, bottom: 10, left: 55, right: 20};
            graphConfig.grid = {y: {show: true}};

            return graphConfig;
        }

        function loadData(data) {
            $scope.data = data;

            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoTable(data) {
            $scope.rows = [];

            if ($scope.energiesoorten.length > 0) {

                for (var i = 0; i < data.length; i++) {
                    var row = {};

                    var label = LocalizationService.getFullMonths()[data[i].maand - 1];

                    row[""] = label;

                    var rowTotal = null;

                    for (var j = 0; j < $scope.energiesoorten.length; j++) {

                        var rowLabel = ($scope.energiesoorten[j].charAt(0).toUpperCase() + $scope.energiesoorten[j].slice(1));
                        var value = data[i][$scope.energiesoorten[j] + '-' + $scope.soort];

                        var rowValue = '';
                        if (value != null) {
                            rowValue = GrafiekService.formatWithUnitLabel($scope.soort, $scope.energiesoorten, value);

                            if (rowTotal == null) { rowTotal = 0; }
                            rowTotal += value;
                        }
                        row[rowLabel] = rowValue;
                    }

                    if ($scope.energiesoorten.length > 1 && rowTotal != null) {
                        row["Totaal"] = GrafiekService.formatWithUnitLabel($scope.soort, $scope.energiesoorten, rowTotal);
                    }

                    $scope.rows.push(row);
                }
            }

            if ($scope.rows.length > 0) {
                $scope.cols = Object.keys($scope.rows[0]);
            } else {
                $scope.cols = [];
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

        function transformServerdata(serverresponses) {
            var result = [];

            for (var i = 0; i < $scope.energiesoorten.length; i++) {
                var serverdataForEnergiesoort = serverresponses[i].data;

                for (var j = 0; j < serverdataForEnergiesoort.length; j++) {

                    var dataOnMaand = getByMaand(result, serverdataForEnergiesoort[j].maand);

                    if (dataOnMaand == null) {
                        dataOnMaand = {};
                        dataOnMaand['maand'] = serverdataForEnergiesoort[j].maand;
                        dataOnMaand[$scope.energiesoorten[i] + '-kosten'] = serverdataForEnergiesoort[j]['kosten'];
                        dataOnMaand[$scope.energiesoorten[i] + '-verbruik'] = serverdataForEnergiesoort[j]['verbruik'];
                        result.push(dataOnMaand);
                    } else {
                        dataOnMaand[$scope.energiesoorten[i] + '-kosten'] = serverdataForEnergiesoort[j]['kosten'];
                        dataOnMaand[$scope.energiesoorten[i] + '-verbruik'] = serverdataForEnergiesoort[j]['verbruik'];
                    }
                }
            }
            return result;
        }

        function getByMaand(data, maand) {
            var result = null;
            for (var i = 0; i < data.length; i++) {
                if (data[i].maand == maand) {
                    result = data[i];
                    break;
                }
            }
            return result;
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
                        $log.error("ERROR: " + JSON.stringify(response));
                        LoadingIndicatorService.stopLoading();
                    }
                );
            }
        }
    }
})();
