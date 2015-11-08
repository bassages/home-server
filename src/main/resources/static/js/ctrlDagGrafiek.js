'use strict';

angular.module('appHomecontrol.dagGrafiekController', [])

    .controller('DagGrafiekController', ['$scope', '$http', '$log', '$routeParams', function($scope, $http, $log, $routeParams) {
        var oneDay = 24 * 60 * 60 * 1000;
        var halfDay = 12 * 60 * 60 * 1000;

        $scope.chart = null;
        $scope.config = {};

        // By default, today is the last day in the graph
        $scope.to = new Date();
        $scope.to.setHours(0,0,0,0);

        $scope.from = new Date($scope.to);
        $scope.from.setDate($scope.from.getDate() - 6);

        var numberOfDaysInPeriod = (($scope.to.getTime() - $scope.from.getTime()) / 1000 / 60 / 60 / 24) + 1;
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
                    $scope.from = new Date(e.date);
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

        $scope.previousDay = function() {
            var previousFrom = new Date($scope.from);
            previousFrom.setDate($scope.from.getDate() - 1);
            $scope.from = previousFrom;

            var previousTo = new Date($scope.to);
            previousTo.setDate($scope.to.getDate() - 1);
            $scope.to = previousTo;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.to);
            $scope.showGraph();
        };

        $scope.nextDay = function() {
            var nextFrom = new Date($scope.from);
            nextFrom.setDate($scope.from.getDate() + 1);
            $scope.from = nextFrom;

            var nextTo = new Date($scope.to);
            nextTo.setDate($scope.to.getDate() + 1);
            $scope.to = nextTo;

            applyDatePickerUpdatesInAngularScope = false;
            theDatepicker.datepicker('setDate', $scope.from);
            $scope.showGraph();
        };

        $scope.dateChanged = function() {
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

        function setDataColor() {
            // Dirty fix to set opacity...
            $('.c3-area-watt').attr('style', 'fill: rgb(31, 119, 180); opacity: 0.8;');
        }

        // TODO: https://github.com/mbostock/d3/wiki/Localization#locale_timeFormat
        // TODO: http://stackoverflow.com/questions/24385582/localization-of-d3-js-d3-locale-example-of-usage

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

        $scope.showGraph = function() {

            //var graphDataUrl = 'rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.from.getTime() + '/' + $scope.to.getTime() + '?subPeriodLength=' + subPeriodLength;
            //$log.info('URL: ' + graphDataUrl);

            //$http.get(graphDataUrl).success(function(data) {
            //if (data) {
            //    var length = data.length;
            //    for (var i=0; i<length; i++) {
            //        var subPeriodEnd = data[i].dt + (subPeriodLength - 1);
            //        data.push({dt: subPeriodEnd, watt: data[i].watt});
            //    }
            //}

            var tickValues = getTicksForEveryDayInPeriod();

            var graphConfig = {};
            graphConfig.bindto = '#chart';
            graphConfig.onresized = setDataColor;
            graphConfig.data = {};
            graphConfig.data.keys = {x: "dt", value: ["kWh"]};

            graphConfig.data.json = [];

            var dt = $scope.to.getTime();
            for (var i=0; i<numberOfDaysInPeriod; i++) {
                var date = new Date(dt);
                $log.debug('Add value for ' + date);
                graphConfig.data.json.push({dt: dt, kWh: date.getDate()});
                dt = dt - oneDay;
            }

            graphConfig.data.types= {"kWh": "bar"};
            graphConfig.data.empty = {label: {text: "Gegevens worden opgehaald..."}};

            var xMin = new Date($scope.from.getTime()) - halfDay;
            var xMax = new Date($scope.to.getTime() + halfDay);

            graphConfig.axis = {};
            graphConfig.axis.x = {type: "timeseries", tick: {format: "%a %d-%m", values: tickValues, centered: true, multiline: true, width: 35}, min: xMin, max: xMax, padding: {left: 0, right:10}};
            graphConfig.axis.y = {label: {text: "kWh", position: "outer-middle"}};

            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 0.8}};
            graphConfig.point = { show: false};
            graphConfig.transition = { duration: 0};
            graphConfig.grid = {y: {show: true}};
            graphConfig.tooltip = {show: false};
            graphConfig.padding = {top: 0, right: 5, bottom: 40, left: 50};
            graphConfig.interaction= {enabled: false};

            $scope.chart = c3.generate(graphConfig);

            setDataColor();
            //});
        }
    }]);
