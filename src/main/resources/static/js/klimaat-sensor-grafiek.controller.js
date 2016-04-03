(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatSensorGrafiekController', KlimaatSensorGrafiekController);

    KlimaatSensorGrafiekController.$inject = ['$scope', '$http', '$log', 'LoadingIndicatorService', 'LocalizationService', 'ErrorMessageService'];

    function KlimaatSensorGrafiekController($scope, $http, $log, LoadingIndicatorService, LocalizationService, GrafiekService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.selection = Date.today();

            //GrafiekService.manageGraphSize($scope);
            LocalizationService.localize();

            getDataFromServer();
        }

        $scope.getD3DateFormat = function() {
            return '%a %d-%m-%Y';
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

        function getEmptyGraphConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: {top: 10, bottom: 20, left: 50, right: 20}
            }
        }

        function getGraphConfig(graphData) {
            var graphConfig = {};
            var tickValues = getTicksForEveryHourInPeriod($scope.selection, getTo());

            graphConfig.bindto = '#chart';

            graphConfig.data = {type: 'spline', json: graphData, keys: {x: "datumtijd", value: ["temperatuur"]}};
            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -90},
                min: $scope.selection,
                max: getTo(),
                padding: {left: 0, right: 10}
            };
            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 1}};
            graphConfig.transition = {duration: 0};
            graphConfig.padding = {top: 10, bottom: 45, left: 50, right: 20};
            graphConfig.grid = {y: {show: true}};

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
            //GrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo() {
            return $scope.selection.clone().add({ days: 1});
        }

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();
            loadDataIntoGraph([]);

            var graphDataUrl = 'rest/klimaat/history/' + $scope.selection.getTime() + '/' + getTo().getTime();
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

