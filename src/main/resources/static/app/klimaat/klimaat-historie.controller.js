(function() {
    'use strict';

    angular
        .module('app')
        .controller('KlimaatHistorieController', KlimaatHistorieController);

    KlimaatHistorieController.$inject = ['$scope', '$http', '$q', '$routeParams', '$log', '$uibModal', 'LoadingIndicatorService', 'KlimaatHistorieService', 'ErrorMessageService'];

    function KlimaatHistorieController($scope, $http, $q, $routeParams, $log, $uibModal, LoadingIndicatorService, KlimaatSensorGrafiekService, ErrorMessageService) {
        var vm = this;

        vm.navigate = navigate;
        vm.getD3DateFormat = getD3DateFormat;
        vm.getFormattedSelectedDates = getFormattedSelectedDates;
        vm.isMaxSelected = isMaxSelected;
        vm.isSelectionNavigationPossible = isSelectionNavigationPossible;
        vm.openDateSelectionDialog = openDateSelectionDialog;

        activate();

        function activate() {
            vm.selection = [Date.today()];
            vm.sensortype = $routeParams.sensortype;
            vm.data = [];

            KlimaatSensorGrafiekService.manageChartSize($scope, showChart, showTable);

            getDataFromServer();
        }

        function showChart() {
            if (!vm.showChart) {
                vm.showTable = false;
                vm.showChart = true;
                loadDataIntoChart(vm.data);
            }
        }

        function showTable() {
            if (!vm.showTable) {
                vm.showChart = false;
                vm.showTable = true;
                loadDataIntoTable(vm.data);
            }
        }

        function getD3DateFormat() {
            return '%a %d-%m-%Y';
        }

        function getFormattedSelectedDates() {
            return _.map(vm.selection, function(date) { return d3.time.format(vm.getD3DateFormat())(date); }).join(', ');
        }

        function isMaxSelected() {
            return vm.selection.length === 1 && Date.today().getTime() === vm.selection[0].getTime();
        }

        function isSelectionNavigationPossible() {
            return vm.selection.length === 1;
        }

        function navigate(numberOfPeriods) {
            vm.selection[0].setDate(vm.selection[0].getDate() + numberOfPeriods);
            getDataFromServer();
        }

        function openDateSelectionDialog() {
            var modalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'app/multiple-dates-selection-dialog.html',
                backdrop: 'static',
                controller: 'MultipleDateSelectionController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    selectedDates: function() { return vm.selection; },
                    datepickerOptions: function() { return { maxDate: Date.today(), datepickerMode: 'day', minMode: 'day' }; },
                    selectedDateFormat: function() { return 'EEE. dd-MM-yyyy'; }
                }
            });
            modalInstance.result.then(
            function(selectedDates) {
                vm.selection = selectedDates;
                getDataFromServer();
            }, function() {
                $log.info('Multiple Date Selection dialog was closed');
            });
        }

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

                    if (klimaatValue !== null && (typeof max === 'undefined' || klimaatValue > max)) {
                        max = klimaatValue;
                    }
                    if (klimaatValue !== null && klimaatValue > 0 && (typeof min === 'undefined' || klimaatValue < min)) {
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

            return {
                avg: avg, min: min, max: max
            };
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
                var titleOfTimeColumn = "";
                row[titleOfTimeColumn] = d3.time.format('%H:%M')(new Date(data[i].datumtijd));

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

            vm.rows = rows;
            vm.cols = cols;
        }

        function sortTableColumns(a, b) {
            var result = 0;
            if (a !== b) {
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
                if (vm.sensortype === 'temperatuur') {
                    result = numbro(value).format('0.00') + '\u2103';
                } else if (vm.sensortype === 'luchtvochtigheid') {
                    result = numbro(value).format('0.0') + '%';
                } else {
                    $log.warn('Unexpected sensortype: ' + vm.sensortype);
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
            for (var i = 0; i < vm.selection.length; i++) {
                value.push(d3.time.format('%d-%m-%Y')(vm.selection[i]));
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
                        return d3.time.format(vm.getD3DateFormat())(theDate);
                    }
                }
            };

            var statistics = getStatistics(chartData);
            chartConfig.grid.y.lines = KlimaatSensorGrafiekService.getStatisticsChartLines(statistics, formatWithUnitLabel);

            return chartConfig;
        }

        function loadData(data) {
            vm.data = data;
            if (vm.showChart) {
                loadDataIntoChart(data);
            }
            if (vm.showTable) {
                loadDataIntoTable(data);
            }
        }

        function loadDataIntoChart(data) {
            var chartConfig;
            if (data.length === 0) {
                chartConfig = getEmptyChartConfig();
            } else {
                chartConfig = getChartConfig(data);
            }
            vm.chart = c3.generate(chartConfig);
            KlimaatSensorGrafiekService.setChartHeightMatchingWithAvailableWindowHeight(vm.chart);
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
                    var datumtijdValue = serverresponse.data[j][vm.sensortype];

                    var row = getOrCreateCombinedRow(result, datumtijd.clone().set({ day: getFixedDate().getDate(), month: getFixedDate().getMonth(), year: getFixedDate().getFullYear()}));
                    row[datumtijdKey] = datumtijdValue;
                }
            }
            return result;
        }

        function getOrCreateCombinedRow(currentRows, datumtijd) {
            var row = null;

            for (var i = 0; i < currentRows.length; i++) {
                if (currentRows[i].datumtijd.getTime() === datumtijd.getTime()) {
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

            if (vm.selection.length > 0) {

                LoadingIndicatorService.startLoading();

                var requests = [];

                for (var i = 0; i < vm.selection.length; i++) {
                    var from = vm.selection[i];
                    var to = getTo(from);
                    var defaultSensorCode = 'WOONKAMER';
                    var dataUrl = 'api/klimaat/' + defaultSensorCode + '?from=' + from.toString('yyyy-MM-dd') + '&to=' + to.toString('yyyy-MM-dd');
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
