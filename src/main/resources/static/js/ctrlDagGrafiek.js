'use strict';

// TODO: https://github.com/eternicode/bootstrap-datepicker/issues/615

angular.module('appHomecontrol.dagGrafiekController', [])

    .controller('DagGrafiekController', ['$scope', '$http', '$log', function($scope, $http, $log) {
        var oneDay = 24 * 60 * 60 * 1000;
        var halfDay = 12 * 60 * 60 * 1000;

        $scope.chart = null;
        $scope.config = {};

        // By default, today is the last day in the graph
        $scope.to = new Date();
        $scope.to.setHours(0,0,0,0);

        $scope.from = new Date($scope.to);
        $scope.from.setDate($scope.from.getDate() - 6);

        var numberOfDaysInPeriod = (($scope.to.getTime() - $scope.from.getTime()) / oneDay) + 1;
        $log.debug('Period: ' + $scope.from + ' - ' + $scope.to);
        $log.debug('numberOfDaysInPeriod: ' + numberOfDaysInPeriod);

        var applyDatePickerUpdatesInAngularScope = false;
        var theDatepicker = $('.datepicker');
        theDatepicker.datepicker({
            autoclose: true,
            todayBtn: "linked",
            calendarWeeks: true,
            todayHighlight: true,
            endDate: "0d",
            language:"nl",
            format: "dd-mm-yyyy"
        });
        theDatepicker.on('changeDate', function(e) {
            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.to = new Date(e.date);
                    var from = new Date($scope.to);
                    from.setDate(from.getDate() - 6);
                    $scope.from = from;
                    $log.debug("changeDate() " + $scope.from + ' - ' + $scope.to);
                    $scope.showGraph();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });
        theDatepicker.datepicker('setDate', $scope.to);

        $scope.isTodaySelected = function() {
            var result = false;

            var today = new Date();
            today.setHours(0,0,0,0);

            if ($scope.to) {
                result = today.getTime() == $scope.to.getTime();
            }
            return result;
        };

        $scope.navigateDay = function(numberOfDays) {
            var nextFrom = new Date($scope.from);
            nextFrom.setDate($scope.from.getDate() + numberOfDays);
            $scope.from = nextFrom;

            var nextTo = new Date($scope.to);
            nextTo.setDate($scope.to.getDate() + numberOfDays);
            $scope.to = nextTo;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.from);
            $scope.showGraph();
        };

        function getTicksForEveryDayInPeriod() {
            // Add one tick for every day
            var tickValues = [];
            for (var i = 0; i < numberOfDaysInPeriod; i++) {
                var tickValue = $scope.from.getTime() + (i * oneDay);
                tickValues.push(tickValue);
                $log.debug('Add tick for ' + new Date(tickValue));
            }
            return tickValues;
        }

        var myFormatters = d3.locale({
            "decimal": ",",
            "thousands": ".",
            "grouping": [3],
            "currency": ["€", ""],
            "dateTime": "%a %b %e %X %Y",
            "date": "%d-%m-%Y",
            "time": "%H:%M:%S",
            "periods": ["AM", "PM"],
            "days": ["Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"],
            "shortDays": ["Zo", "Ma", "Di", "Wo", "Do", "Vr", "Za"],
            "months": ["Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"],
            "shortMonths": ["Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"]
        });
        d3.time.format = myFormatters.timeFormat;
        d3.format = myFormatters.numberFormat;

        $scope.showGraph = function() {

            var graphDataUrl = 'rest/elektriciteit/verbruikPerDag/' + $scope.from.getTime() + '/' + $scope.to.getTime();
            $log.info('URL: ' + graphDataUrl);

            var total = 0;
            var average = 0;

            $http.get(graphDataUrl).success(function(data) {
                var tickValues = getTicksForEveryDayInPeriod();

                var length = data.length;
                for (var i=0; i<length; i++) {
                    total += data[i].kWh;
                }
                average = total/length;

                var xMin = new Date($scope.from.getTime()) - halfDay;
                var xMax = new Date($scope.to.getTime() + halfDay);

                var graphConfig = {};
                graphConfig.bindto = '#chart';
                graphConfig.data = {};
                graphConfig.data.keys = {x: 'dt', value: ['kWh', 'euro']};
                graphConfig.data.axes = {'euro': 'y2'};

                graphConfig.data.json = data;
                graphConfig.data.type = 'bar';
                graphConfig.data.types = {'euro': 'bar'};

                graphConfig.axis = {};
                graphConfig.axis.x = {type: "timeseries", tick: {format: "%a %d-%m", values: tickValues, centered: true, multiline: true, width: 35}, min: xMin, max: xMax, padding: {left: 0, right:10}};
                graphConfig.axis.y = {label: {text: "Verbruik", position: "outer-middle"}, tick: {format: function (d) { return d + ' kWh'; }}};
                graphConfig.axis.y2 = {label: {text: 'Kosten', position: "outer-middle"}, show: true, tick: {format: d3.format("$.2f")}};
                graphConfig.legend = {show: false};
                graphConfig.bar = {width: {ratio: 0.8}};
                graphConfig.point = { show: false};
                graphConfig.transition = { duration: 0};
                graphConfig.grid = {y: {show: true}};
                graphConfig.tooltip = {show: false};
                graphConfig.padding = {top: 0, right: 70, bottom: 40, left: 70};
                graphConfig.interaction= {enabled: false};
                if (average > 0) {
                    graphConfig.grid.y.lines = [{value: average, text: '', class: 'gemiddelde'}];
                }
                $scope.chart = c3.generate(graphConfig);
                $scope.chart.hide(['euro']);
            });
        }
    }]);
