(function() {
    'use strict';

    angular
        .module('app')
        .controller('MeterstandenController', MeterstandenController);

    MeterstandenController.$inject = ['$scope', '$log', 'MeterstandenService', 'LocalizationService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function MeterstandenController($scope, $log, MeterstandenService, LocalizationService, LoadingIndicatorService, ErrorMessageService) {
        activate();

        function activate() {
            LocalizationService.localize();
            $scope.selection = Date.today().clearTime().moveToFirstDayOfMonth();
            getDataFromServer();
        }

        $scope.isMaxSelected = function() {
            return Date.today().getMonth() == $scope.selection.getMonth() && Date.today().getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection.setMonth($scope.selection.getMonth() + numberOfPeriods);
            datepicker.datepicker('setDate', $scope.selection);
            getDataFromServer();
        };

        $scope.getD3DateFormat = function() {
            return '%B %Y';
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

        datepicker.on('changeDate', function(e) {
            if (!Date.equals(e.date, $scope.selection)) {
                $log.info("changeDate event from datepicker. Selected date: " + e.date);

                $scope.$apply(function() {
                    $scope.selection = new Date(e.date);
                    getDataFromServer();
                });
            }
        });

        function getDataFromServer() {
            LoadingIndicatorService.startLoading();

            var van = $scope.selection.getTime();
            var totEnMet = $scope.selection.clone().moveToLastDayOfMonth().setHours(23, 59, 59, 999);

            MeterstandenService.getMeterstandenPerDagInPeriod(van, totEnMet)
                .then(
                function successCallback(response) {
                    $scope.meterstandenPerDag = response.data;
                    LoadingIndicatorService.stopLoading();
                },
                function errorCallback(response) {
                    $log.error(JSON.stringify(response));
                    LoadingIndicatorService.stopLoading();
                    ErrorMessageService.showMessage("Kon meterstanden niet ophalen");
                }
            );
        }
    }
})();
