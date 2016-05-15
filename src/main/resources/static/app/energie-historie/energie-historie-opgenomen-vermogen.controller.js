(function() {
    'use strict';

    angular
        .module('app')
        .controller('OpgenomenVermogenGrafiekController', OpgenomenVermogenGrafiekController);

    OpgenomenVermogenGrafiekController.$inject = ['$scope', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'EnergieHistorieService', 'ErrorMessageService'];

    function OpgenomenVermogenGrafiekController($scope, $http, $log, LoadingIndicatorService, LocalizationService, EnergieHistorieService, ErrorMessageService) {
        var SIX_MINUTES_IN_MILLISECONDS = 6 * 60 * 1000;

        activate();

        function activate() {
            $scope.selection = Date.today();
            $scope.energiesoort = 'stroom';
            $scope.period = 'opgenomen-vermogen'; // Strange but true for this controller :-o
            $scope.supportedsoorten = [{'code': 'stroom', 'omschrijving': 'Watt'}];
            $scope.soort = 'stroom';

            EnergieHistorieService.manageGraphSize($scope);
            LocalizationService.localize();

            getDataFromServer();
        }

        $scope.getD3DateFormat = function() {
            return '%a %d-%m-%Y';
        };

        $scope.hideEnergieSoorten = function() {
            return true;
        };

        $scope.showOpgenomenVermogen = function() {
            return true;
        };

        $scope.hideUur = function() {
            return true;
        };

        $scope.hideDag = function() {
            return true;
        };

        $scope.hideMaand = function() {
            return true;
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            autoclose: true, todayBtn: "linked", calendarWeeks: true, todayHighlight: true, endDate: "0d", language:"nl", daysOfWeekHighlighted: "0,6",
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

        function getTicksForEveryHourInPeriod(from, to) {
            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;

            var tickValues = [];
            for (var i = 0; i <= numberOfHoursInDay; i++) {
                var tickValue = from.getTime() + (i * 60 * 60 * 1000);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function addSubPeriodEnd(data) {
            var length = data.length;
            for (var i = 0; i < length; i++) {
                var subPeriodEnd = data[i].dt + (SIX_MINUTES_IN_MILLISECONDS - 1);
                data.push({dt: subPeriodEnd, watt: data[i].watt});
            }
        }

        function getStatistics(graphData) {
            var min, max, avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < graphData.length; i++) {
                var data = graphData[i].watt;

                if (data != null && (typeof max=='undefined' || data > max)) {
                    max = data;
                }
                if (data != null && data > 0 && (typeof min=='undefined' || data < min)) {
                    min = data;
                }
                if (data != null && data > 0) {
                    total += data;
                    nrofdata += 1;
                }
            }

            if (nrofdata > 0) {
                avg = total / nrofdata;
            }
            return {avg: avg, min: min, max: max};
        }

        function getGraphPadding() {
            return {top: 10, bottom: 25, left: 55, right: 20};
        }

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 30, left: 50, right: 20}
            }
        }

        function getGraphConfig(graphData) {
            var graphConfig = {};
            var tickValues = getTicksForEveryHourInPeriod($scope.selection, getTo());

            graphConfig.bindto = '#chart';

            graphConfig.data = {json: graphData, keys: {x: "dt", value: ["watt"]}, types: {"watt": "area"}};
            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -30},
                min: $scope.selection, max: getTo(),
                padding: {left: 0, right: 10}
            };
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 1}};
            graphConfig.point = {show: false};
            graphConfig.transition = {duration: 0};
            graphConfig.tooltip = {show: false};
            graphConfig.padding = getGraphPadding();
            graphConfig.grid = {y: {show: true}};

            var statistics = getStatistics(graphData);

            var lines = [];
            if (statistics.avg) {
                lines.push({value: statistics.avg, text: 'Gemiddelde: ' + Math.round(statistics.avg) + ' watt', class: 'avg', position: 'middle'});
            }
            if (statistics.min) {
                lines.push({value: statistics.min, text: 'Laagste: ' + statistics.min + ' watt', class: 'min', position: 'start'});
            }
            if (statistics.max) {
                lines.push({value: statistics.max, text: 'Hoogste: ' + statistics.max + ' watt', class: 'max'});
            }
            graphConfig.grid.y.lines = lines;

            addSubPeriodEnd(graphData);

            return graphConfig;
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
            EnergieHistorieService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo() {
            return $scope.selection.clone().add({ days: 1});
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();
            loadDataIntoGraph([]);

            var graphDataUrl = 'rest/' + $scope.energiesoort + '/opgenomen-vermogen-historie/' + $scope.selection.getTime() + '/' + getTo().getTime() + '?subPeriodLength=' + SIX_MINUTES_IN_MILLISECONDS;
            $log.info('Getting data from URL: ' + graphDataUrl);

            $http({method: 'GET', url: graphDataUrl})
                .then(
                    function successCallback(response) {
                        loadDataIntoGraph(response.data);
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
})();
