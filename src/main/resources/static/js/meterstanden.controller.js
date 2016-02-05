(function() {
    'use strict';

    angular
        .module('app')
        .controller('MeterstandenController', MeterstandenController);

    MeterstandenController.$inject = ['$scope', '$log', 'MeterstandenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MeterstandenController($scope, $log, MeterstandenService, LoadingIndicatorService, ErrorMessageService) {

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var van = $scope.selection.getTime();
            var totEnMet = (new Date($scope.selection)).moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            MeterstandenService.getMeterstandenPerDagInPeriod(van, totEnMet)
                .then(
                    function successCallback(response) {
                        $scope.meterstandenPerDag = response.data;
                        LoadingIndicatorService.stopLoading();
                    },
                    function errorCallback(response) {
                        LoadingIndicatorService.stopLoading();
                        ErrorMessageService.showMessage("Kon meterstanden niet ophalen");
                    }
                );
        }

        function activate() {
            $scope.selection = (new Date()).clearTime().moveToFirstDayOfMonth();
            getDataFromServer();
        }

        $scope.isMaxSelected = function() {
            return (new Date()).getMonth() == $scope.selection.getMonth() && (new Date()).getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = new Date($scope.selection);
            $scope.selection.setMonth($scope.selection.getMonth() + numberOfPeriods);

            applyDatePickerUpdatesInAngularScope = false;
            datepicker.datepicker('setDate', $scope.selection);

            getDataFromServer();
        };

        $scope.getD3DateFormat = function() {
            return '%b. %Y';
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
        var applyDatePickerUpdatesInAngularScope = true;

        datepicker.on('changeDate', function(e) {
            $log.info("changeDate event from datepicker. Selected date: " + e.date);

            if (applyDatePickerUpdatesInAngularScope) {
                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    getDataFromServer();
                });
            }
            applyDatePickerUpdatesInAngularScope = true;
        });

        activate();
    }
})();
