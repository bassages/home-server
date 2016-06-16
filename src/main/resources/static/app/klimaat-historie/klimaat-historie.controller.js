(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatHistorieController', KlimaatHistorieController);

    KlimaatHistorieController.$inject = ['$scope', '$http', '$q', '$routeParams', '$log', 'LoadingIndicatorService', 'LocalizationService', 'KlimaatHistorieService', 'ErrorMessageService'];

    function KlimaatHistorieController($scope, $http, $q, $routeParams, $log, LoadingIndicatorService, LocalizationService, KlimaatSensorGrafiekService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.selection = [Date.today()];
            $scope.soort = $routeParams.soort;

            KlimaatSensorGrafiekService.manageGraphSize($scope);
            LocalizationService.localize();

            getDataFromServer();
        }

        $scope.getD3DateFormat = function() {
            return '%a %d-%m-%Y';
        };

        $scope.isMultidateAllowed = function() {
            return true;
        };

        $scope.getMultidateSeparator = function() {
            return ', ';
        };

        var datepicker = $('.datepicker');
        datepicker.datepicker({
            autoclose: false, todayBtn: "true", clearBtn: true, calendarWeeks: true, todayHighlight: true, endDate: "0d", language:"nl", daysOfWeekHighlighted: "0,6", multidate: true, multidateSeparator: $scope.getMultidateSeparator(),
            format: {
                toDisplay: function (date, format, language) {
                    return d3.time.format($scope.getD3DateFormat())(date);
                },
                toValue: function (date, format, language) {
                    return (date == '0d' ? new Date() : d3.time.format($scope.getD3DateFormat()).parse(date));
                }
            }
        });

        datepicker.datepicker('setDates', $scope.selection);

        datepicker.on('changeDate', function(e) {
            if (!isSelectionEqual(e.dates, $scope.selection)) {
                $scope.$apply(function() {
                    $scope.selection = e.dates;
                    getDataFromServer();
                });
            }
        });
        datepicker.on('clearDate', function(e) {
            $scope.$apply(function() {
                $scope.selection = [];
                getDataFromServer();
            });
        });

        function isSelectionEqual(oldSelection, newSelection) {
            var result = true;
            if (oldSelection.length == newSelection.length) {
                for (var i = 0; i < oldSelection.length; i++) {
                    if (!containsDate(newSelection, oldSelection[i])) {
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
            }
            return result;
        }

        function containsDate(dates, date) {
            for (var i = 0; i < dates.length; i++) {
                if (dates[i].equals(date)) {
                    return true;
                }
            }
            return false;
        }

        $scope.isMaxSelected = function() {
            return $scope.selection.length == 1 && Date.today().getTime() == $scope.selection[0].getTime();
        };

        $scope.isSelectionNavigationPossible = function() {
            return $scope.selection.length == 1;
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection[0].setDate($scope.selection[0].getDate() + numberOfPeriods);
            datepicker.datepicker('setDates', $scope.selection);
            getDataFromServer();
        };

        function getTicksForEveryHourInDay() {
            var from = getFixedDate();
            var to = getTo(from);

            var numberOfHoursInDay = ((to - from) / 1000) / 60 / 60;

            var tickValues = [];
            for (var i = 0; i <= numberOfHoursInDay; i++) {
                var tickValue = from.getTime() + (i * 60 * 60 * 1000);
                tickValues.push(tickValue);
            }
            return tickValues;
        }

        function getStatistics(graphData) {
            var min, max, avg;

            var total = 0;
            var nrofdata = 0;

            for (var i = 0; i < graphData.length; i++) {
                Object.keys(graphData[i]).forEach(function(key, index) {
                    if (key != 'datumtijd') {
                        var value = graphData[i][key];

                        if (value != null && (typeof max=='undefined' || value > max)) {
                            max = value;
                        }
                        if (value != null && value > 0 && (typeof min=='undefined' || value < min)) {
                            min = value;
                        }
                        if (value != null && value > 0) {
                            total += value;
                            nrofdata += 1;
                        }
                    }
                });
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
                padding: getGraphPadding()
            }
        }

        function getFixedDate() {
            return Date.parse('01-01-2016');
        }

        function loadDataIntoTable(data) {
            var rows = [];
            var cols = [];

            for (var i = 0; i < data.length; i++) {
                var row = {};
                row[""] = d3.time.format('%H:%M')(new Date(data[i].datumtijd));

                Object.keys(data[i]).forEach(function(key, index) {
                    if (key != 'datumtijd') {
                        var value = data[i][key];
                        row[key] = formatWithUnitLabel(value);
                    }
                });
                rows.push(row);
            }
            if (rows.length > 0) {
                cols = Object.keys(rows[0]);
            }

            cols.sort(sortTableColumns);

            $scope.rows = rows;
            $scope.cols = cols;
        }

        function sortTableColumns(a, b) {
            var result = 0;
            if (a != b) {
                if (a == '') {
                    result = -1;
                } else {
                    var dateA = d3.time.format('%d-%m-%Y').parse(a);
                    var dateB = d3.time.format('%d-%m-%Y').parse(b);
                    result = Date.compare(dateA, dateB);
                }
            }
            return result;
        }

        function formatWithUnitLabel(value) {
            var result = null;
            if (value != null) {
                if ($scope.soort == 'temperatuur') {
                    result = numbro(value).format('0.00') + '\u2103';
                } else if ($scope.soort == 'luchtvochtigheid') {
                    result = numbro(value).format('0.0') + '%';
                } else {
                    $log.warn('Unexpected soort: ' + $scope.soort);
                    result = value;
                }
            }
            return result;
        }

        function getGraphConfig(graphData) {
            var graphConfig = {};
            var tickValues = getTicksForEveryHourInDay();

            graphConfig.bindto = '#chart';

            var value  = [];
            for (var i = 0; i < $scope.selection.length; i++) {
                value.push(d3.time.format('%d-%m-%Y')($scope.selection[i]));
            }

            graphConfig.data = {type: 'spline', json: graphData, keys: {x: "datumtijd", value: value}};

            graphConfig.line = {connectNull: true};

            graphConfig.axis = {};
            graphConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -30},
                min: getFixedDate(), max: getTo(getFixedDate()),
                padding: {left: 0, right: 10}
            };
            graphConfig.axis.y = {tick: {format: formatWithUnitLabel }};

            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 1}};
            graphConfig.transition = {duration: 0};
            graphConfig.padding = getGraphPadding();
            graphConfig.grid = {y: {show: true}};

            graphConfig.tooltip = {
                format: {
                    name: function (name, ratio, id, index) {
                        var theDate = d3.time.format('%d-%m-%Y').parse(name);
                        return d3.time.format($scope.getD3DateFormat())(theDate);
                    }
                }
            };

            var statistics = getStatistics(graphData);
            graphConfig.grid.y.lines = KlimaatSensorGrafiekService.getStatisticsGraphLines(statistics, formatWithUnitLabel);

            return graphConfig;
        }

        function loadData(data) {
            loadDataIntoGraph(data);
            loadDataIntoTable(data);
        }

        function loadDataIntoGraph(data) {
            var graphConfig;
            if (data.length == 0) {
                graphConfig = getEmptyGraphConfig();
            } else {
                graphConfig = getGraphConfig(data);
            }
            $scope.chart = c3.generate(graphConfig);
            KlimaatSensorGrafiekService.setGraphHeightMatchingWithAvailableWindowHeight($scope.chart);
        }

        function getTo(from) {
            return from.clone().add({days: 1});
        }

        function transformServerdata(serverresponses) {
            var result = [];

            for (var i = 0; i < serverresponses.length; i++) {
                var serverresponse = serverresponses[i]; // Values on a specific date

                for (var j = 0; j < serverresponse.data.length; j++) {
                    var datumtijd = new Date(serverresponse.data[j].datumtijd);

                    var datumtijdKey = d3.time.format('%d-%m-%Y')(datumtijd);
                    var datumtijdValue = serverresponse.data[j][$scope.soort];

                    var row = getOrCreateCombinedRow(result, datumtijd.clone().set({ day: getFixedDate().getDate(), month: getFixedDate().getMonth(), year: getFixedDate().getFullYear()}));
                    row[datumtijdKey] = datumtijdValue;
                }
            }
            return result;
        }

        function getOrCreateCombinedRow(currentRows, datumtijd) {
            var row = null;

            for (var i = 0; i < currentRows.length; i++) {
                if (currentRows[i].datumtijd.getTime() == datumtijd.getTime()) {
                    row = currentRows[i];
                    break;
                }
            }
            if (row == null) {
                row = {};
                row['datumtijd'] = datumtijd;
                currentRows.push(row);
            }
            return row;
        }

        function getDataFromServer() {
            loadData([]);
            if ($scope.selection.length > 0) {

                var loading = LoadingIndicatorService.startLoading();

                var requests = [];

                for (var i = 0; i < $scope.selection.length; i++) {
                    var dataUrl = 'rest/klimaat/get/' + $scope.selection[i].getTime() + '/' + getTo($scope.selection[i]).getTime();
                    $log.info('Getting data from URL: ' + dataUrl);
                    requests.push( $http({method: 'GET', url: dataUrl}) );
                }

                $q.all(requests).then(
                    function successCallback(response) {
                        loadData(transformServerdata(response));
                        loading.close();
                    },
                    function errorCallback(response) {
                        $log.error(JSON.stringify(response));
                        loading.close();
                        ErrorMessageService.showMessage("Er is een fout opgetreden bij het ophalen van de gegevens");
                    }
                );
            }
        }
    }
})();
