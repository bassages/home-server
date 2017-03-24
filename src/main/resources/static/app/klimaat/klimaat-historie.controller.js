(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatHistorieController', KlimaatHistorieController);

    KlimaatHistorieController.$inject = ['$scope', '$http', '$q', '$routeParams', '$log', 'LoadingIndicatorService', 'KlimaatHistorieService', 'ErrorMessageService'];

    function KlimaatHistorieController($scope, $http, $q, $routeParams, $log, LoadingIndicatorService, KlimaatSensorGrafiekService, ErrorMessageService) {
        activate();

        function activate() {
            $scope.selection = [Date.today()];
            $scope.sensortype = $routeParams.sensortype;
            $scope.data = [];

            KlimaatSensorGrafiekService.manageChartSize($scope);

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

        function getStatistics(chartData) {
            var min, max, avg;

            var sumOfAllKlimaatValues = 0;
            var nrOfKlimaatValues = 0;

            for (var i = 0; i < chartData.length; i++) {
                var klimaatValuesOnSameTimeOfMultipleDays = chartData[i];

                // The next array will contain the formatted date(s) and an item with value 'datumtijd'
                var formattedDates = Object.keys(klimaatValuesOnSameTimeOfMultipleDays);
                // Remove the datumtijd value, so that only formatted dates remain
                removeItemFromList(formattedDates, 'datumtijd');

                for (var j = 0; j < formattedDates.length; j++) {
                    var formattedDate = formattedDates[j];

                    var klimaatValue = klimaatValuesOnSameTimeOfMultipleDays[formattedDate];

                    if (klimaatValue !== null && (typeof max=='undefined' || klimaatValue > max)) {
                        max = klimaatValue;
                    }
                    if (klimaatValue !== null && klimaatValue > 0 && (typeof min=='undefined' || klimaatValue < min)) {
                        min = klimaatValue;
                    }
                    if (klimaatValue !== null && klimaatValue > 0) {
                        sumOfAllKlimaatValues += klimaatValue;
                        nrOfKlimaatValues += 1;
                    }
                }
            }
            if (nrOfKlimaatValues > 0) {
                avg = sumOfAllKlimaatValues / nrOfKlimaatValues;
            }
            return {avg: avg, min: min, max: max};
        }

        function removeItemFromList(list, itemToRemove) {
            list.splice(list.indexOf(itemToRemove), 1);
            return list;
        }

        function getChartPadding() {
            return {top: 10, bottom: 25, left: 55, right: 20};
        }

        function getEmptyChartConfig() {
            return {
                data: {json: {}},
                legend: {show: false},
                axis: {x: {tick: {values: []}}, y: {tick: {values: []}}},
                padding: getChartPadding()
            };
        }

        function getFixedDate() {
            return Date.parse('01-01-2016');
        }

        function loadDataIntoTable(data) {
            $log.debug('loadDataIntoTable', data.length);

            var rows = [];
            var cols = [];

            for (var i = 0; i < data.length; i++) {
                var klimaatValuesOnSameTimeOfMultipleDays = data[i];

                var row = {};
                row[""] = d3.time.format('%H:%M')(new Date(data[i].datumtijd));

                var formattedDates = Object.keys(klimaatValuesOnSameTimeOfMultipleDays);
                formattedDates = removeItemFromList(formattedDates, 'datumtijd');

                for (var j = 0; j < formattedDates.length; j++) {
                    var formattedDate = formattedDates[j];
                    var sensorValue = klimaatValuesOnSameTimeOfMultipleDays[formattedDate];
                    row[formattedDate] = formatWithUnitLabel(sensorValue);
                }
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
                if (a === '') {
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
            if (value !== null) {
                if ($scope.sensortype == 'temperatuur') {
                    result = numbro(value).format('0.00') + '\u2103';
                } else if ($scope.sensortype == 'luchtvochtigheid') {
                    result = numbro(value).format('0.0') + '%';
                } else {
                    $log.warn('Unexpected sensortype: ' + $scope.sensortype);
                    result = value;
                }
            }
            return result;
        }

        function getChartConfig(chartData) {
            var chartConfig = {};
            var tickValues = getTicksForEveryHourInDay();

            chartConfig.bindto = '#chart';

            var value  = [];
            for (var i = 0; i < $scope.selection.length; i++) {
                value.push(d3.time.format('%d-%m-%Y')($scope.selection[i]));
            }

            chartConfig.data = {type: 'spline', json: chartData, keys: {x: "datumtijd", value: value}};

            chartConfig.line = {connectNull: true};

            chartConfig.axis = {};
            chartConfig.axis.x = {
                type: "timeseries",
                tick: {format: "%H:%M", values: tickValues, rotate: -30},
                min: getFixedDate(), max: getTo(getFixedDate()),
                padding: {left: 0, right: 10}
            };
            chartConfig.axis.y = {tick: {format: formatWithUnitLabel }};

            chartConfig.legend = {show: false};
            chartConfig.bar = {width: {ratio: 1}};
            chartConfig.transition = {duration: 0};
            chartConfig.padding = getChartPadding();
            chartConfig.grid = {y: {show: true}};

            chartConfig.tooltip = {
                format: {
                    name: function (name, ratio, id, index) {
                        var theDate = d3.time.format('%d-%m-%Y').parse(name);
                        return d3.time.format($scope.getD3DateFormat())(theDate);
                    }
                }
            };

            var statistics = getStatistics(chartData);
            chartConfig.grid.y.lines = KlimaatSensorGrafiekService.getStatisticsChartLines(statistics, formatWithUnitLabel);

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

        function loadDataIntoChart(data) {
            $log.debug('loadDataIntoChart', data.length);

            var chartConfig;
            if (data.length === 0) {
                chartConfig = getEmptyChartConfig();
            } else {
                chartConfig = getChartConfig(data);
            }
            $scope.chart = c3.generate(chartConfig);
            KlimaatSensorGrafiekService.setChartHeightMatchingWithAvailableWindowHeight($scope.chart);
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
                    var datumtijdValue = serverresponse.data[j][$scope.sensortype];

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
            if (row === null) {
                row = {};
                row.datumtijd = datumtijd;
                currentRows.push(row);
            }
            return row;
        }

        function getDataFromServer() {
            loadData([]);
            if ($scope.selection.length > 0) {

                LoadingIndicatorService.startLoading();

                var requests = [];

                for (var i = 0; i < $scope.selection.length; i++) {
                    var dataUrl = 'api/klimaat?from=' + $scope.selection[i].getTime() + '&to=' + (getTo($scope.selection[i]).getTime() - 1);
                    requests.push( $http({method: 'GET', url: dataUrl}) );
                }

                $q.all(requests).then(
                    function successCallback(response) {
                        loadData(transformServerdata(response));
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
    }
})();
