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
            $scope.dateformat = 'MMMM yyyy';
            getDataFromServer();
        }

        $scope.isMaxSelected = function() {
            return Date.today().getMonth() == $scope.selection.getMonth() && Date.today().getFullYear() == $scope.selection.getFullYear();
        };

        $scope.navigate = function(numberOfPeriods) {
            $scope.selection = $scope.selection.clone().add(numberOfPeriods).months();
            getDataFromServer();
        };

        $scope.datepickerPopupOptions = {
            datepickerMode: 'month',
            minMode: 'month',
            maxDate: Date.today()
        };

        $scope.datepickerPopup = {
            opened: false
        };

        $scope.toggleDatepickerPopup = function() {
            $scope.datepickerPopup.opened = !$scope.datepickerPopup.opened;
        };

        $scope.selectionChange = function() {
            getDataFromServer();
        };

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
                    $log.error(angular.toJson(response));
                    LoadingIndicatorService.stopLoading();
                    ErrorMessageService.showMessage("Kon meterstanden niet ophalen");
                }
            );
        }
    }
})();
